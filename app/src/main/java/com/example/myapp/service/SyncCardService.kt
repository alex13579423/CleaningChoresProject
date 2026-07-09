package com.example.myapp.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.example.myapp.data.ChoreRepository

class SyncCardService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return hexToBytes("6A82")

        if (isSelectAid(commandApdu)) {
            val repository = ChoreRepository.getInstance(applicationContext)
            val json = repository.getSyncJson()
            NfcSyncManager.notifySuccess()
            return json.toByteArray() + hexToBytes("9000")
        }

        return hexToBytes("6A82")
    }

    override fun onDeactivated(reason: Int) {}

    private fun isSelectAid(apdu: ByteArray): Boolean {
        return apdu.size >= 5 && apdu[1] == 0xA4.toByte() && apdu[2] == 0x04.toByte()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in 0 until hex.length step 2) {
            val firstDigit = Character.digit(hex[i], 16)
            val secondDigit = Character.digit(hex[i + 1], 16)
            result[i / 2] = ((firstDigit shl 4) + secondDigit).toByte()
        }
        return result
    }
}
