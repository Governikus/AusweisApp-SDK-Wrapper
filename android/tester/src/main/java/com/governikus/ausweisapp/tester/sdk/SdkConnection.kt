/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.core.content.IntentCompat.getParcelableExtra
import com.governikus.ausweisapp.tester.sdk.jsonobjects.Command
import com.governikus.ausweisapp.tester.sdk.jsonobjects.SetApiLevel
import com.governikus.ausweisapp2.IAusweisApp2Sdk
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SdkConnection internal constructor(
    private val callback: SdkCallback,
) : ServiceConnection {
    private val tag = "SDK Demo"
    val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private var aa2SDKCallback: IAusweisApp2Sdk? = null

    override fun onServiceConnected(
        className: ComponentName,
        service: IBinder,
    ) {
        try {
            aa2SDKCallback = IAusweisApp2Sdk.Stub.asInterface(service)?.apply { connectSdk(callback) }
        } catch (e: ClassCastException) {
            Log.d(tag, "Unable to perform binder cast")
            e.printStackTrace()
        } catch (e: RemoteException) {
            Log.d(tag, "Unable to perform binder cast")
            e.printStackTrace()
        } finally {
            send(SetApiLevel(3), SetApiLevel::class.java)
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        // Not implemented yet.
    }

    fun <T : Command> send(
        message: T,
        clazz: Class<T>,
    ) {
        send(moshi.adapter(clazz).toJson(message))
    }

    fun send(message: String) {
        val sdk = aa2SDKCallback ?: return

        Log.d("send()", message)
        sdk.send(callback.sessionID, message)
    }

    fun send(intent: Intent) {
        val sdk = aa2SDKCallback ?: return

        try {
            val tag = getParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag::class.java) ?.also { Log.i("Tag", toString()) }
            sdk.updateNfcTag(callback.sessionID, tag)
        } catch (e: RemoteException) {
            Log.e(tag, "An unexpected error occured while setting the nfc tag.", e)
        }
    }
}
