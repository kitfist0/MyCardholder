package my.cardholder.util.logo

import coil.transform.Transformation
import android.graphics.*
import coil.size.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave

class PaddedRoundedTransformation(
    private val paddingPx: Int,
    private val cornerRadiusPx: Int,
    private val backgroundColor: Int = Color.WHITE,
) : Transformation {

    override val cacheKey: String =
        "${javaClass.name}-$paddingPx-$cornerRadiusPx-$backgroundColor"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // Logos are shown in squares, and a wide or tall logo would be stretched to fill one.
        // Padded to a square instead, it keeps its proportions.
        val outputSize = maxOf(input.width, input.height) + (paddingPx * 2)
        val outputWidth = outputSize
        val outputHeight = outputSize
        val imageLeft = (outputSize - input.width) / 2f
        val imageTop = (outputSize - input.height) / 2f

        // Create output bitmap
        val output = createBitmap(outputWidth, outputHeight)
        val canvas = Canvas(output)

        // Draw white background with rounded corners
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = backgroundColor

        val backgroundRect = RectF(0f, 0f, outputWidth.toFloat(), outputHeight.toFloat())
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
}
