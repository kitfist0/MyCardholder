package my.cardholder.util.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

private const val JPEG_QUALITY = 90

fun ImageProxy.toBitmap(): Bitmap? {
    if (format != ImageFormat.YUV_420_888 || planes.size < 3) {
        return null
    }
    val jpegBytes = ByteArrayOutputStream().use { stream ->
        val yuvImage = YuvImage(toNv21ByteArray(), ImageFormat.NV21, width, height, null)
        yuvImage.compressToJpeg(Rect(0, 0, width, height), JPEG_QUALITY, stream)
        stream.toByteArray()
    }
    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size) ?: return null
    val rotationDegrees = imageInfo.rotationDegrees
    if (rotationDegrees == 0) {
        return bitmap
    }
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun ImageProxy.toNv21ByteArray(): ByteArray {
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val nv21 = ByteArray(width * height * 3 / 2)
    var offset = 0

    val yBuffer = yPlane.buffer
    val yRowStride = yPlane.rowStride
    for (row in 0 until height) {
        val rowStart = row * yRowStride
        for (col in 0 until width) {
            nv21[offset++] = yBuffer.get(rowStart + col)
        }
    }

    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride
    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            val uvIndex = row * uvRowStride + col * uvPixelStride
            nv21[offset++] = vBuffer.get(uvIndex)
            nv21[offset++] = uBuffer.get(uvIndex)
        }
    }

    return nv21
}
