/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.tech.IsoDep

internal class ForegroundDispatcher(
    private val activity: Activity,
    val callback: (Intent) -> Unit,
) {
    private val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
    private val mReaderCallback =
        ReaderCallback { tag ->
            if (tag.techList.asList().contains(IsoDep::class.java.name)) {
                val nfcIntent = Intent()
                nfcIntent.putExtra(NfcAdapter.EXTRA_TAG, tag)
                callback(nfcIntent)
            }
        }

    fun enable() {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.enableReaderMode(activity, mReaderCallback, flags, null)
    }

    fun disable() {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.disableReaderMode(activity)
    }
}
