/*
 * Copyright (c) 2024-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class WebSocketServer(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : CoroutineScope by MainScope() {
    private var webSocketSession: WebSocketServerSession? = null
    private val server =
        embeddedServer(factory = Netty, port = 8080) {
            install(plugin = WebSockets)
            routing {
                webSocket(path = "/") {
                    webSocketSession = this
                    onConnected?.invoke()
                    try {
                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue
                            val receivedText = frame.readText()
                            onNewMessage?.invoke(receivedText)
                        }
                    } catch (_: ClosedReceiveChannelException) {
                        println(message = "onClose ${closeReason.await()}")
                    } finally {
                        webSocketSession = null
                    }
                }
            }
        }

    var ip: String = "/"
    var isStarted = false
    var onStarted: ((isStarted: Boolean) -> Unit)? = null
    var onNewMessage: ((message: String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    fun send(message: String) {
        launch(context = ioDispatcher) {
            webSocketSession?.send(frame = Frame.Text(text = message))
        }
    }

    init {
        server.application.monitor.subscribe(definition = ApplicationStarted) {
            val wifiIp = getLocalWifiIpAddress(context = context)
            launch(context = mainDispatcher) {
                ip = "Websocket IP ${wifiIp?.hostAddress} Port: 8080"
                isStarted = true
                onStarted?.invoke(isStarted)
            }
        }
        server.application.monitor.subscribe(definition = ApplicationStopped) {
            launch(context = mainDispatcher) {
                ip = "/"
                isStarted = false
                onStarted?.invoke(isStarted)
            }
        }
    }

    fun start() {
        if (isStarted) {
            return
        }

        launch(context = ioDispatcher) {
            server.start(wait = true)
        }
    }

    fun stop() {
        if (!isStarted) {
            return
        }

        launch(context = ioDispatcher) {
            server.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
        }
    }

    private fun getLocalWifiIpAddress(context: Context): InetAddress? {
        val wifiManager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager

        @Suppress("DEPRECATION")
        var ipAddress = wifiManager.connectionInfo.ipAddress
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray: ByteArray = ipAddress.toBigInteger().toByteArray()
        return try {
            InetAddress.getByAddress(ipByteArray)
        } catch (_: UnknownHostException) {
            null
        }
    }
}
