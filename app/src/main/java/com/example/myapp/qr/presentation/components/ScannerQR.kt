package com.example.myapp.qr.presentation.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapp.qr.data.manager.QrCryptoManager
import com.example.myapp.qr.data.manager.QrImageAnalyzer
import com.example.myapp.qr.data.manager.RaptorQStreamAnalyzer
import java.util.concurrent.Executors

private const val TAG = "ScannerDebugger"

@Composable
fun ScannerQR(
    onDataReceived: (String) -> Unit,
    onProgress: (Float) -> Unit,
    qrImageAnalyzer: QrImageAnalyzer,
    qrCryptoManager: QrCryptoManager,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    val analyzer = remember(onDataReceived, onProgress) {
        RaptorQStreamAnalyzer(
            mainExecutor = mainExecutor,
            onProgress = onProgress,
            onDataReceived = onDataReceived,
            qrImageAnalyzer = qrImageAnalyzer,
            qrCryptoManager = qrCryptoManager,
        )
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}
