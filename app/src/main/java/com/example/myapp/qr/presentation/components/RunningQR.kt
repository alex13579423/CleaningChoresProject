package com.example.myapp.qr.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.example.myapp.qr.data.manager.QrCryptoManager
import com.example.myapp.qr.data.util.CompressionUtils
import com.example.myapp.qr.presentation.util.QrGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

@Composable
fun RunningQR(
    rawJsonData: String,
    modifier: Modifier = Modifier,
    qrCryptoManager: QrCryptoManager,
    qrGenerator: QrGenerator,
    frameRate: Int = 5,
    qrColor: Color = MaterialTheme.colorScheme.primary,
    qrBackgroundColor: Color = Color.White
) {
    val frameRateMs = 1.seconds.inWholeMilliseconds / frameRate

    val encryptedState = produceState<ByteArray?>(initialValue = null, rawJsonData) {
        value = withContext(Dispatchers.Default) {
            val compressed = CompressionUtils.compressString(rawJsonData)
            qrCryptoManager.encryptBytes(compressed)
        }
    }

    val encryptedData = encryptedState.value

    if (encryptedData != null) {
        val fgColorInt = qrColor.toArgb()
        val bgColorInt = qrBackgroundColor.toArgb()

        val qrFlow = remember(encryptedData) {
            qrGenerator.generateChunkedStream(
                encryptedData = encryptedData,
                frameRateMs = frameRateMs,
                fgColorInt = fgColorInt,
                bgColorInt = bgColorInt
            )
        }

        val qrBitmap by qrFlow.collectAsState(initial = null)

        Box(modifier = modifier.aspectRatio(1f)) {
            qrBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(
                        image = bitmap.asImageBitmap(),
                        filterQuality = FilterQuality.None
                    ),
                    contentDescription = "Animating QR Stream",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}