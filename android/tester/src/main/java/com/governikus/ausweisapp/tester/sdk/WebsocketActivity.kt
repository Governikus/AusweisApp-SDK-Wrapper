/*
 * Copyright (c) 2024-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.governikus.ausweisapp.tester.wrapper.databinding.ActivityWebsocketBinding
import java.io.IOException
import java.util.Locale

class WebsocketActivity : AppCompatActivity() {
    private var sdkConnection: SdkConnection? = null
    private val sdkCallback =
        object : SdkCallback() {
            @Throws(RemoteException::class)
            override fun receive(pJson: String) {
                addLineOfText(text = pJson)
                webSocketServer.send(message = pJson)
            }

            @Throws(RemoteException::class)
            override fun sdkDisconnected() {
                addLineOfText(text = "SDK Disconnect")
            }
        }

    private val webSocketServer =
        WebSocketServer(context = this).also {
            it.onStarted = { isStarted ->
                if (isStarted) {
                    addShortLineOfText(text = "Server is started")
                    addShortLineOfText(text = "Write ':help' for the command list")
                    addLineOfText(text = it.ip)
                } else {
                    addShortLineOfText(text = "Server is stopped")
                }
            }
            it.onConnected = {
                addLineOfText(text = "Client is connected")
            }
            it.onNewMessage = { message ->
                handleWebSocketMessage(webSocketServer = it, message = message)
            }
        }

    private lateinit var viewBinding: ActivityWebsocketBinding
    private lateinit var dispatcher: ForegroundDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWebsocketBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        dispatcher = ForegroundDispatcher(activity = this) { nfcIntent -> sdkConnection?.send(intent = nfcIntent) }
        viewBinding.websocketLogView.movementMethod = ScrollingMovementMethod()
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketServer.stop()
        sdkConnection?.let {
            unbindService(it)
        }
    }

    private fun addLineOfText(text: String) {
        runOnUiThread {
            Log.i("WebSocket", text)
            viewBinding.websocketLogView.append("$text\n\n")
        }
    }

    private fun addShortLineOfText(text: String) {
        runOnUiThread {
            Log.i("WebSocket", text)
            viewBinding.websocketLogView.append("$text\n")
        }
    }

    /**
     * Establishes AusweisApp service connection.
     *
     * @param aa2package
     * @throws RemoteException
     */
    fun startAA2SDK() {
        if (sdkConnection != null) {
            Log.d("startAA2", "start AA2")
            addLineOfText(text = "AA2 is already connected")
            return
        }
        sdkConnection =
            SdkConnection(callback = sdkCallback).also { connection ->
                val serviceIntent = Intent("com.governikus.ausweisapp2.START_SERVICE")
                serviceIntent.setPackage(applicationContext.packageName)
                bindService(serviceIntent, connection, BIND_AUTO_CREATE)
                connection.send(intent = intent)
            }
    }

    /**
     * Starts the websocket service.
     *
     * @param view
     * @throws IOException
     */
    @Suppress("UNUSED_PARAMETER")
    fun startWebsocket(view: View) {
        if (webSocketServer.isStarted) {
            addLineOfText(text = "Websocket already started")
            return
        }
        webSocketServer.start()
        viewBinding.startWebsocket.isEnabled = false
    }

    public override fun onResume() {
        super.onResume()
        dispatcher.enable()
    }

    public override fun onPause() {
        super.onPause()
        dispatcher.disable()
    }

    private fun handleWebSocketMessage(
        webSocketServer: WebSocketServer,
        message: String,
    ) {
        val sdkConnection = sdkConnection
        addLineOfText(text = message)
        if (message.startsWith(prefix = ":")) {
            val splitMessage =
                message
                    .substring(startIndex = 1)
                    .lowercase(locale = Locale.getDefault())
                    .split(" ", limit = 2)
            when (splitMessage.firstOrNull()?.trim()) {
                "open" -> {
                    if (sdkConnection != null) {
                        webSocketServer.send(message = "Already connected")
                        return
                    }
                    webSocketServer.send(message = "Connect to SDK..")
                    startAA2SDK()
                }

                "close" -> {
                    if (sdkConnection == null) {
                        webSocketServer.send(message = "Not connected yet")
                        return
                    }
                    webSocketServer.send(message = "Disconnect from SDK...")
                    unbindService(sdkConnection)
                    this@WebsocketActivity.sdkConnection = null
                }

                "help" -> {
                    webSocketServer.send(message = "---------Help---------")
                    webSocketServer.send(message = ":open - Open connection to SDK")
                    webSocketServer.send(message = ":close - Close connection to SDK")
                    webSocketServer.send(message = ":help - This help")
                }

                else -> {
                    addLineOfText(text = "Unknown command")
                    webSocketServer.send(message = "Unknown command")
                }
            }
        } else {
            if (sdkConnection == null) {
                webSocketServer.send(message = "Not connected yet! Try :help for commands.")
                return
            }

            Log.d("handleMessage()", "$message: $sdkConnection")
            sdkConnection.send(message = message)
        }
    }

    companion object {
        /**
         * Creates a [WebsocketActivity] instance and start it.
         */
        fun callActivity(context: Context) {
            val i = Intent(context.applicationContext, WebsocketActivity::class.java)
            context.startActivity(i)
        }
    }
}
