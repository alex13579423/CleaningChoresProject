package com.example.myapp.qr.data.manager

import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.myapp.qr.data.util.CompressionUtils
import java.util.concurrent.Executor

class ChunkStreamAnalyzer(
    private val mainExecutor: Executor,
    private val onProgress: (Float) -> Unit,
    private val onDataReceived: (String) -> Unit,
    private val qrImageAnalyzer: QrImageAnalyzer,
    private val qrCryptoManager: QrCryptoManager,
) : ImageAnalysis.Analyzer {
    private val tag = "ChunkStreamAnalyzer"

    private var isComplete = false
    private val collectedChunks = mutableMapOf<Int, String>()
    private var totalExpected = 0

    override fun analyze(imageProxy: ImageProxy) {
        if (isComplete) {
            imageProxy.close()
            return
        }

        qrImageAnalyzer.processImageProxy(imageProxy) { text ->
            processText(text)
        }
    }

    private fun processText(text: String) {
        Log.d(tag, "Raw QR text received: $text")
        try {
            val parts = text.split("|", limit = 2)
            if (parts.size != 2) {
                Log.w(tag, "Invalid format: expected '|' separator")
                return
            }

            val header = parts[0].split("/")
            if (header.size != 2) {
                Log.w(tag, "Invalid header format: expected '/' separator")
                return
            }

            val index = header[0].toIntOrNull() ?: return
            val total = header[1].toIntOrNull() ?: return
            val data = parts[1]

            Log.d(tag, "Processing chunk: $index/$total (Data length: ${data.length})")

            if (totalExpected == 0) {
                totalExpected = total
                Log.i(tag, "Set total expected chunks to $total")
            }

            if (!collectedChunks.containsKey(index)) {
                collectedChunks[index] = data
                val progress = collectedChunks.size.toFloat() / totalExpected.toFloat()
                Log.i(tag, "Collected chunk $index. Progress: ${collectedChunks.size}/$totalExpected (${(progress * 100).toInt()}%)")
                mainExecutor.execute { onProgress(progress) }

                if (collectedChunks.size == totalExpected) {
                    Log.i(tag, "All chunks collected. Reassembling...")
                    val fullBase64 = StringBuilder()
                    for (i in 0 until totalExpected) {
                        fullBase64.append(collectedChunks[i] ?: "")
                    }

                    try {
                        val base64String = fullBase64.toString()
                        Log.d(tag, "Full Base64 length: ${base64String.length}")
                        val encrypted = Base64.decode(base64String, Base64.NO_WRAP)
                        Log.d(tag, "Decoded ByteArray length: ${encrypted.size}")
                        
                        val decrypted = qrCryptoManager.decryptBytes(encrypted)
                        Log.d(tag, "Decrypted ByteArray length: ${decrypted.size}")
                        
                        val json = CompressionUtils.decompressGzip(decrypted)
                        Log.i(tag, "Successfully decompressed JSON (length: ${json.length})")

                        isComplete = true
                        mainExecutor.execute { onDataReceived(json) }
                    } catch (e: Exception) {
                        Log.e(tag, "Decompression/Decryption Failed. Resetting buffer.", e)
                        collectedChunks.clear()
                        totalExpected = 0
                        mainExecutor.execute { onProgress(0f) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Frame Processing Error", e)
        }
    }
}