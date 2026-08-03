package com.example.myapp.qr.presentation.util

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import qrcode.raw.ErrorCorrectionLevel
import qrcode.raw.QRCodeDataType
import qrcode.raw.QRCodeProcessor
import kotlin.math.ceil

class QrGenerator {
    fun generateQrBitmap(content: String, qrColor: Int, qrBackgroundColor: Int): Bitmap {
        val processor = QRCodeProcessor(content, errorCorrectionLevel = ErrorCorrectionLevel.LOW, dataType = QRCodeDataType.DEFAULT)
        val modules = processor.encode()
        val height = modules.size
        val width = modules[0].size
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (modules[y][x].dark) qrColor else qrBackgroundColor
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    fun generateChunkedStream(
        encryptedData: ByteArray,
        frameRateMs: Long,
        fgColorInt: Int,
        bgColorInt: Int
    ): Flow<Bitmap> = flow {
        val base64Data = Base64.encodeToString(encryptedData, Base64.NO_WRAP)
        val chunkSize = 150
        val totalChunks = ceil(base64Data.length / chunkSize.toFloat()).toInt()

        val chunks = (0 until totalChunks).map { i ->
            val end = minOf((i + 1) * chunkSize, base64Data.length)
            val chunkStr = base64Data.substring(i * chunkSize, end)
            "$i/$totalChunks|$chunkStr"
        }

        var currentIndex = 0
        while (true) {
            val bitmap = generateQrBitmap(chunks[currentIndex], fgColorInt, bgColorInt)
            emit(bitmap)
            currentIndex = (currentIndex + 1) % chunks.size
            delay(frameRateMs)
        }
    }.flowOn(Dispatchers.Default)
}