/*
 * Copyright (c) 2024-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.RemoteException
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.governikus.ausweisapp.tester.sdk.jsonobjects.Accept
import com.governikus.ausweisapp.tester.sdk.jsonobjects.Continue
import com.governikus.ausweisapp.tester.sdk.jsonobjects.Message
import com.governikus.ausweisapp.tester.sdk.jsonobjects.Reader
import com.governikus.ausweisapp.tester.sdk.jsonobjects.RunAuth
import com.governikus.ausweisapp.tester.sdk.jsonobjects.SetPin
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.databinding.ActivityTesterBinding
import okio.IOException

/**
 * SDK Tester activity supports two modes to use SDK (as integrated Dependency and as External Application)
 * to do so you have to the set the package name of the service intent to the package name of the application
 * which has integrated the sdk as a dependency.
 * Otherwise if you want to use the AusweisApp you need to set the package name to
 * 'com.governikus.ausweisapp2' this requires an installed AusweisApp.
 */
class TesterActivity : AppCompatActivity() {
    private enum class State {
        UNDEFINED,
        AUTH,
        PIN,
        HAS_SENT,
    }

    private val localCallback = LocalCallback()
    private lateinit var dispatcher: ForegroundDispatcher
    private var sdkConnection: SdkConnection? = null
    private var currentState = State.UNDEFINED
    private lateinit var viewBinding: ActivityTesterBinding
    private var resultUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTesterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        dispatcher = ForegroundDispatcher(activity = this) { nfcIntent -> sdkConnection?.send(intent = nfcIntent) }
        viewBinding.logView.movementMethod = ScrollingMovementMethod()

        createServiceConnection()
    }

    override fun onDestroy() {
        super.onDestroy()

        sdkConnection?.let {
            unbindService(it)
        }
    }

    /**
     * Creates the connection to the sdk and binds the service.
     */
    private fun createServiceConnection() {
        addLineOfText(text = "Tester Mode")
        sdkConnection =
            SdkConnection(callback = localCallback).also { connection ->
                val serviceIntent = Intent("com.governikus.ausweisapp2.START_SERVICE")
                serviceIntent.setPackage(applicationContext.packageName)
                bindService(serviceIntent, connection, BIND_AUTO_CREATE)
                connection.send(intent = intent)
            }
    }

    private fun addLineOfText(text: String) {
        runOnUiThread {
            Log.i("TesterMode", text)
            viewBinding.logView.append(text + "\n\n")
        }
    }

    /**
     * Finish operation.
     * @param view
     */
    @Suppress("UNUSED_PARAMETER")
    fun clickFinish(view: View?) {
        finish()
    }

    /**
     * Run authentication.
     * @param view
     * @throws RemoteException
     */
    @Suppress("UNUSED_PARAMETER")
    fun sendRunAuth(view: View) {
        parseAndShow(url = null, ok = true)
        sdkConnection?.send(command = RunAuth(), clazz = RunAuth::class.java)
    }

    /**
     * Send accept command.
     * @throws RemoteException
     */
    fun sendAccept() {
        sdkConnection?.send(command = Accept(), clazz = Accept::class.java)
    }

    /**
     * Send set pin command.
     * @throws RemoteException
     */
    fun sendSetPin() {
        val length = viewBinding.pin.text.length
        if (length == 0) {
            sdkConnection?.send(command = SetPin(value = null), clazz = SetPin::class.java)
        } else {
            val pin = CharArray(size = length)
            viewBinding.pin.text.getChars(0, length, pin, 0)
            sdkConnection?.send(command = SetPin(value = pin), clazz = SetPin::class.java)
            pin.fill(element = '\u0000')
        }
    }

    fun sendContinue() {
        sdkConnection?.send(command = Continue(), clazz = Continue::class.java)
    }

    private fun parseAndShow(
        url: String?,
        ok: Boolean,
    ) {
        runOnUiThread {
            resultUrl = url
            viewBinding.resultButton.isEnabled = resultUrl != null

            val color =
                if (ok) {
                    if (resultUrl != null) {
                        ResourcesCompat.getColor(resources, R.color.colorTrue, null)
                    } else {
                        Color.TRANSPARENT
                    }
                } else {
                    ResourcesCompat.getColor(resources, R.color.colorFalse, null)
                }
            viewBinding.resultButton.setBackgroundColor(color)
        }
    }

    private inner class LocalCallback : SdkCallback() {
        private fun handleReceive(json: String) {
            addLineOfText(text = json)
            val message = sdkConnection?.moshi?.adapter(Message::class.java)?.fromJson(json) ?: return
            if (message.msg == "AUTH") {
                currentState = State.UNDEFINED
                message.result?.let { result ->
                    if (result.minor == null) { // when no minor exist expect major = ok
                        parseAndShow(url = message.url, ok = true)
                        return
                    }
                    parseAndShow(url = null, ok = false)
                    return
                }
                currentState = State.AUTH
            }
            if (currentState == State.AUTH) {
                if (message.msg == "ACCESS_RIGHTS") {
                    sendAccept()
                } else if (message.msg == "ENTER_PIN") {
                    sendSetPin()
                }
            }

            if (message.msg == "READER") {
                handleReaderMessage(json = json)
            }

            if (message.msg == "PAUSE") {
                addLineOfText(text = "Received message PAUSE with cause ${message.cause.orEmpty()}")
                sendContinue()
            }
        }

        override fun receive(json: String) {
            try {
                handleReceive(json = json)
            } catch (e: IOException) {
                Log.e("SDK DEMO", e.toString())
            }
        }

        override fun sdkDisconnected() {
            addLineOfText(text = "SDK Disconnect")
        }
    }

    fun handleReaderMessage(json: String) {
        val reader = sdkConnection?.moshi?.adapter(Reader::class.java)?.fromJson(json) ?: return
        val card = reader.card
        if (card != null) {
            if (card.retryCounter == null && card.inoperative == null && card.deactivated == null) {
                addLineOfText(text = "Unknown card detected")
            } else {
                addLineOfText(text = "eID card detected")
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        dispatcher.enable()
    }

    public override fun onPause() {
        super.onPause()
        dispatcher.disable()
    }

    @Suppress("UNUSED_PARAMETER")
    public fun openResult(view: View?) {
        if (resultUrl != null) {
            startActivity(Intent(Intent.ACTION_VIEW, resultUrl?.toUri()))
        }
    }

    companion object {
        /**
         * Creates a [TesterActivity] instance and start it.
         */
        fun callActivity(context: Context) {
            val i = Intent(context.applicationContext, TesterActivity::class.java)
            context.startActivity(i)
        }
    }
}
