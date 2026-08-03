//package com.example.myapp.qr.data.manager
//
//import android.util.Log
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageProxy
//import com.example.myapp.qr.data.util.CompressionUtils
//import io.github.andreypfau.raptorq.Decoder
//import java.nio.ByteBuffer
//import java.util.concurrent.Executor
//import kotlin.math.ceil
//
//class RaptorQStreamAnalyzer(
//    private val mainExecutor: Executor,
//    private val onProgress: (Float) -> Unit,
//    private val onDataReceived: (String) -> Unit,
//    private val qrImageAnalyzer: QrImageAnalyzer,
//    private val qrCryptoManager: QrCryptoManager,
//) : ImageAnalysis.Analyzer {
//    private val tag = "RaptorQStreamAnalyzer"
//
//    private var decoder: Decoder? = null
//    private var isComplete = false
//    private var validPacketsCount = 0
//    private var totalPacketsNeeded = 1
//
//    override fun analyze(imageProxy: ImageProxy) {
//        if (isComplete) {
//            imageProxy.close()
//            return
//        }
//
//        qrImageAnalyzer.processImageProxy(imageProxy) { rawBytes ->
//            processRawBytes(rawBytes)
//        }
//    }
//
//    private fun processRawBytes(rawBytes: ByteArray) {
//        try {
//            if (rawBytes.size < 8) return
//
//            val buffer = ByteBuffer.wrap(rawBytes)
//            val totalSize = buffer.int
//            val packetId = buffer.int
//            val data = ByteArray(rawBytes.size - 8)
//            buffer.get(data)
//
//            if (decoder == null) {
//                val symbolSize = data.size
//                val calculatedPackets = maxOf(1, (ceil(totalSize.toFloat() / symbolSize) * 1.1f).toInt())
//                totalPacketsNeeded = calculatedPackets
//
//                decoder = Decoder(
//                    dataSize = totalSize,
//                    symbolSize = symbolSize
//                )
//            }
//
//            validPacketsCount++
//            val rawProgress = (validPacketsCount.toFloat() / totalPacketsNeeded) * 0.99f
//            val safeProgress = rawProgress.coerceIn(0f, 0.99f)
//
//            mainExecutor.execute { onProgress(safeProgress) }
//
//            val currentDecoder = decoder ?: return
//            val added = currentDecoder.addSymbol(packetId, data)
//
//            if (added) {
//                val result = currentDecoder.decodeFullyToByteArray()
//
//                if (result != null) {
//                    isComplete = true
//                    mainExecutor.execute { onProgress(1.0f) }
//
//                    try {
//                        val resultNotNull = result
//                        val decrypted = qrCryptoManager.decryptBytes(resultNotNull)
//                        val json = CompressionUtils.decompressGzip(decrypted)
//                        mainExecutor.execute { onDataReceived(json) }
//                    } catch (e: Exception) {
//                        Log.e(tag, "Decompression/Decryption Failed", e)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(tag, "Frame Processing Error", e)
//        }
//    }
//}
