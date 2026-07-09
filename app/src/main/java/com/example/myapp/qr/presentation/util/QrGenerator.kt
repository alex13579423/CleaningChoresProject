package com.example.myapp.qr.presentation.util

import android.graphics.Bitmap
import android.util.Base64
import io.github.andreypfau.raptorq.Encoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import qrcode.raw.ErrorCorrectionLevel
import qrcode.raw.QRCodeDataType
import qrcode.raw.QRCodeProcessor
import java.nio.ByteBuffer

class QrGenerator {
    fun generateQrBitmap(
        content: String,
        qrColor: Int,
        qrBackgroundColor: Int
    ): Bitmap {
        val processor = QRCodeProcessor(
            content,
            errorCorrectionLevel = ErrorCorrectionLevel.LOW,
            dataType = QRCodeDataType.DEFAULT
        )

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

    fun generateQrStream(
        encoder: Encoder,
        originalEncryptedSize: Int,
        frameRateMs: Long,
        fgColorInt: Int,
        bgColorInt: Int
    ): Flow<Bitmap> = flow {
        var packetId = 0

        while (true) {
            val symbolData = encoder.encodeToByteArray(packetId)

            val payloadSize = 4 + 4 + symbolData.size
            val payload = ByteBuffer.allocate(payloadSize)
                .putInt(originalEncryptedSize)
                .putInt(packetId)
                .put(symbolData)
                .array()

            val rawDataString = Base64.encodeToString(payload, Base64.NO_WRAP)
            val bitmap = generateQrBitmap(rawDataString, fgColorInt, bgColorInt)

            emit(bitmap)

            packetId++
            delay(frameRateMs)
        }
    }.flowOn(Dispatchers.Default)
}
