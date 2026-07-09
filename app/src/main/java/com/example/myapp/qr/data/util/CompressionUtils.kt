package com.example.myapp.qr.data.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object CompressionUtils {
    fun compressString(data: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { it.write(data.toByteArray()) }
        return outputStream.toByteArray()
    }

    fun decompressGzip(data: ByteArray): String {
        return GZIPInputStream(ByteArrayInputStream(data)).bufferedReader().use { it.readText() }
    }
}
