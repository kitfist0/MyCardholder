package my.cardholder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import javax.inject.Inject

class ImageFileDecoder @Inject constructor(
    private val context: Context,
) {

    private companion object {
        // Barcodes stay readable at this size, while full-resolution photos would waste a lot of memory.
        const val MAX_DECODED_IMAGE_SIZE_PX = 2048
    }

    /**
     * Decodes an image file into a software bitmap (so its pixels can be read), downsampled to at
     * most [MAX_DECODED_IMAGE_SIZE_PX] and rotated upright according to its EXIF orientation.
     */
    fun decodeUprightBitmap(imageUri: String): Bitmap? {
        val uri = imageUri.toUri()
        val contentResolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        var sampleSize = 1
        while (maxOf(
                bounds.outWidth,
                bounds.outHeight
            ) / (sampleSize * 2) >= MAX_DECODED_IMAGE_SIZE_PX
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = contentResolver.openInputStream(uri)
            ?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null

        val rotationDegrees = contentResolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
        if (rotationDegrees == 0) {
            return bitmap
        }
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
