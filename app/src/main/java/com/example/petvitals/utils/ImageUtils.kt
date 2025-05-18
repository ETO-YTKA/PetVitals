package com.example.petvitals.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 1000, maxHeight: Int = 1000): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val ratioBitmap = width.toFloat() / height.toFloat()
    val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight
    if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
    }
    return bitmap.scale(finalWidth, finalHeight)
}

@OptIn(ExperimentalEncodingApi::class)
fun processImageUri(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    val quality = 90

    val resizedBitmap = resizeBitmap(originalBitmap)

    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
    val imageBytes = outputStream.toByteArray()

    return Base64.encode(imageBytes)
}

@OptIn(ExperimentalEncodingApi::class)
fun decodeBase64ToImage(imageString: String): ByteArray {
    return Base64.decode(imageString)
}