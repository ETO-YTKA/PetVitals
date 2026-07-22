package com.example.petvitals.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.roundToInt

private const val DEFAULT_MAX_IMAGE_DIMENSION = 720
private const val DEFAULT_WEBP_QUALITY = 80

internal fun calculateTargetSize(
    sourceWidth: Int,
    sourceHeight: Int,
    maxWidth: Int = DEFAULT_MAX_IMAGE_DIMENSION,
    maxHeight: Int = DEFAULT_MAX_IMAGE_DIMENSION
): Pair<Int, Int> {
    val scale = minOf(
        maxWidth.toDouble() / sourceWidth,
        maxHeight.toDouble() / sourceHeight,
        1.0
    )
    return (sourceWidth * scale).roundToInt().coerceAtLeast(1) to
        (sourceHeight * scale).roundToInt().coerceAtLeast(1)
}

internal fun calculateInSampleSize(
    sourceWidth: Int,
    sourceHeight: Int,
    maxWidth: Int = DEFAULT_MAX_IMAGE_DIMENSION,
    maxHeight: Int = DEFAULT_MAX_IMAGE_DIMENSION
): Int {
    val (targetWidth, targetHeight) = calculateTargetSize(
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        maxWidth = maxWidth,
        maxHeight = maxHeight
    )
    var sampleSize = 1

    while (
        sourceWidth / (sampleSize * 2) >= targetWidth &&
        sourceHeight / (sampleSize * 2) >= targetHeight
    ) {
        sampleSize *= 2
    }

    return sampleSize
}

fun resizeBitmap(
    bitmap: Bitmap,
    maxWidth: Int = DEFAULT_MAX_IMAGE_DIMENSION,
    maxHeight: Int = DEFAULT_MAX_IMAGE_DIMENSION
): Bitmap {
    val (targetWidth, targetHeight) = calculateTargetSize(
        sourceWidth = bitmap.width,
        sourceHeight = bitmap.height,
        maxWidth = maxWidth,
        maxHeight = maxHeight
    )
    return if (targetWidth == bitmap.width && targetHeight == bitmap.height) {
        bitmap
    } else {
        bitmap.scale(targetWidth, targetHeight)
    }
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun processImageUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsInputStream = context.contentResolver.openInputStream(uri)
        ?: throw IOException("Unable to open selected image")
    boundsInputStream.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, bounds)
    }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw IOException("Unable to read selected image dimensions")
    }

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
    }
    val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, decodeOptions)
    } ?: throw IOException("Unable to decode selected image")
    val resizedBitmap = resizeBitmap(decodedBitmap)

    try {
        ByteArrayOutputStream().use { outputStream ->
            val compressed = resizedBitmap.compress(
                balancedWebpFormat(),
                DEFAULT_WEBP_QUALITY,
                outputStream
            )
            if (!compressed) throw IOException("Unable to compress selected image")
            Base64.encode(outputStream.toByteArray())
        }
    } finally {
        if (resizedBitmap !== decodedBitmap) resizedBitmap.recycle()
        decodedBitmap.recycle()
    }
}

private fun balancedWebpFormat(): Bitmap.CompressFormat =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }

@OptIn(ExperimentalEncodingApi::class)
fun decodeBase64ToImage(imageString: String): ByteArray {
    return Base64.decode(imageString)
}
