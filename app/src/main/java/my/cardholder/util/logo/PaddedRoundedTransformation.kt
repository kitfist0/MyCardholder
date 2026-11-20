package my.cardholder.util.logo

import coil.transform.Transformation
import android.graphics.*
import coil.size.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave

/**
 * Puts a logo on a rounded square. The square continues the logo's own background, e.g. it is
 * yellow for a logo on a yellow square, so that no frame of another color is around the logo.
 * A logo without its own background, e.g. a round one, is put on [defaultBackgroundColor].
 */
class PaddedRoundedTransformation(
    private val paddingPx: Int,
    private val cornerRadiusPx: Int,
    private val defaultBackgroundColor: Int = Color.WHITE,
) : Transformation {

    override val cacheKey: String =
        "${javaClass.name}-edge-$paddingPx-$cornerRadiusPx-$defaultBackgroundColor"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // Logos are shown in squares, and a wide or tall logo would be stretched to fill one.
        // Padded to a square instead, it keeps its proportions.
        val outputSize = maxOf(input.width, input.height) + (paddingPx * 2)
        val imageLeft = (outputSize - input.width) / 2f
        val imageTop = (outputSize - input.height) / 2f

        // Create output bitmap
        val output = createBitmap(outputSize, outputSize)
        val canvas = Canvas(output)

        // Draw the background with rounded corners
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = input.edgeColor() ?: defaultBackgroundColor

        val backgroundRect = RectF(0f, 0f, outputSize.toFloat(), outputSize.toFloat())
        canvas.drawRoundRect(
            backgroundRect,
            cornerRadiusPx.toFloat(),
            cornerRadiusPx.toFloat(),
            backgroundPaint
        )

        // Create a mask for the image area (with same rounded corners)
        val imageRect = RectF(
            imageLeft,
            imageTop,
            imageLeft + input.width,
            imageTop + input.height,
        )

        // Save canvas state
        canvas.withSave {

            // Clip to rounded rectangle for the image
            val path = Path().apply {
                addRoundRect(
                    imageRect,
                    cornerRadiusPx.toFloat(),
                    cornerRadiusPx.toFloat(),
                    Path.Direction.CW
                )
            }
            clipPath(path)

            // Draw the image
            drawBitmap(input, imageLeft, imageTop, null)
        }

        return output
    }

    private fun Bitmap.edgeColor(): Int? {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return LogoBackground.edgeColor(pixels, width, height)
    }
}
