/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
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
import com.governikus.ausweisapp.tester.wrapper.databinding.ActivityWebsocketBinding
import java.io.IOException
import java.util.Locale

class WebsocketActivity : AppCompatActivity() {
    private var sdkConnection: SdkConnection? = null
    private val sdkCallback =
        object : SdkCallback() {
            @Throws(RemoteException::class)
            override fun receive(pJson: String) {
                addLineOfText(pJson)
                webSocketServer.send(pJson)
            }

            @Throws(RemoteException::class)
            override fun sdkDisconnected() {
                addLineOfText("SDK Disconnect")
            }
        }

    private var webSocketServer =
        WebSocketServer(this).also {
            it.onStarted = { isStarted ->
                if (isStarted) {
                    addShortLineOfText("Server is started")
                    addShortLineOfText("Write ':help' for the command list")
                    addLineOfText(it.ip)
                } else {
                    addShortLineOfText("Server is stopped")
                }
            }
            it.onConnected = {
                addLineOfText("Client is connected")
            }
            it.onNewMessage = { message ->
                handleWebSocketMessage(it, message)
            }
        }

    private lateinit var viewBinding: ActivityWebsocketBinding
    private lateinit var dispatcher: ForegroundDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWebsocketBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        dispatcher = ForegroundDispatcher(this) { nfcIntent -> sdkConnection?.send(nfcIntent) }
        viewBinding.websocketLogView.movementMethod = ScrollingMovementMethod()
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
     * Establishes AusweisApp2 service connection.
     *
     * @param aa2package
     * @throws RemoteException
     */
    fun startAA2SDK() {
        if (sdkConnection != null) {
            Log.d("startAA2", "start AA2")
            addLineOfText("AA2 is already connected")
            return
        }
        sdkConnection =
            SdkConnection(sdkCallback).also { connection ->
                val serviceIntent = Intent("com.governikus.ausweisapp2.START_SERVICE")
                serviceIntent.setPackage(applicationContext.packageName)
                bindService(serviceIntent, connection, BIND_AUTO_CREATE)
                connection.send(intent)
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
            addLineOfText("Websocket already started")
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
        addLineOfText(message)
        if (message.startsWith(":")) {
            val splitMessage =
                message
                    .substring(1)
                    .lowercase(Locale.getDefault())
                    .split(" ", limit = 2)
            when (splitMessage.firstOrNull()?.trim()) {
                "open" -> {
                    if (sdkConnection != null) {
                        webSocketServer.send("Already connected")
                        return
                    }
                    webSocketServer.send("Connect to SDK..")
                    startAA2SDK()
                }
                "close" -> {
                    if (sdkConnection == null) {
                        webSocketServer.send("Not connected yet")
                        return
                    }
                    webSocketServer.send("Disconnect from SDK...")
                    unbindService(sdkConnection)
                    this@WebsocketActivity.sdkConnection = null
                }
                "help" -> {
                    webSocketServer.send("---------Help---------")
                    webSocketServer.send(":open - Open connection to SDK (optional: com.governikus.ausweisapp2.dev as parameter)")
                    webSocketServer.send(":close - Close connection to SDK")
                    webSocketServer.send(":help - This help")
                }
                else -> {
                    addLineOfText("Unknown command")
                    webSocketServer.send("Unknown command")
                }
            }
        } else {
            if (sdkConnection == null) {
                webSocketServer.send("Not connected yet! Try :help for commands.")
                return
            }

            Log.d("handleMessage()", "$message: $sdkConnection")
            sdkConnection.send(message)
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
