package com.example.myapp.qr.data.manager

import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class QrCryptoManager(
    secretKeyString: String = "cleaning_chores_default_secret_key"
) {
    private val tag = "QrCryptoManager"
    private val secretKey: SecretKeySpec

    init {
        Log.d(tag, "Initializing with key string length: ${secretKeyString.length}")
        // Ensuring the key is exactly 16 bytes by hashing the input string
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(secretKeyString.toByteArray(Charsets.UTF_8))
        secretKey = SecretKeySpec(hash, 0, 16, "AES")
    }

    companion object {
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    fun encryptBytes(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return iv + cipher.doFinal(plaintext)
    }

    fun decryptBytes(combined: ByteArray): ByteArray {
        return try {
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Log.e(tag, "AES-GCM Decryption failed", e)
            throw e
        }
    }
}
