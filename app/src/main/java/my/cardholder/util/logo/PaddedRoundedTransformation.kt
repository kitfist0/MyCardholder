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
        val outputWidth = input.width + (paddingPx * 2)
        val outputHeight = input.height + (paddingPx * 2)

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
            paddingPx.toFloat(),
            paddingPx.toFloat(),
            (paddingPx + input.width).toFloat(),
            (paddingPx + input.height).toFloat()
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
            drawBitmap(input, paddingPx.toFloat(), paddingPx.toFloat(), null)
        }

        return output
    }
}
