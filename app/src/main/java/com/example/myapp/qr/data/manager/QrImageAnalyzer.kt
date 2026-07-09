package com.example.myapp.qr.data.manager

import android.util.Base64
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrImageAnalyzer {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onQrFound: (ByteArray) -> Unit
    ) {
        val image = imageProxy.image
        if (image != null) {
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val rawValue = barcodes.firstOrNull()?.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        try {
                            onQrFound(Base64.decode(rawValue, Base64.NO_WRAP))
                        } catch (_: IllegalArgumentException) { }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
