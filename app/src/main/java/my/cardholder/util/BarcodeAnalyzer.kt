package my.cardholder.util

import android.graphics.ImageFormat.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class BarcodeAnalyzer(
    private val onResult: (result: Result) -> Unit,
) : ImageAnalysis.Analyzer {

    private val yuvFormats = listOf(YUV_420_888, YUV_422_888, YUV_444_888)

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
        )
        setHints(map)
    }

    override fun analyze(image: ImageProxy) {
        // We are using YUV format because, ImageProxy internally uses ImageReader to get the image
        // by default ImageReader uses YUV format unless changed
        if (image.format !in yuvFormats) {
            Log.e("BARCODE_ANALYZER", "Expected YUV, now = ${image.format}")
            return
        }

        val data = image.planes[0].buffer.toByteArray()

        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            // Whenever reader fails to detect a QR code in image
            // it throws NotFoundException
            val result = reader.decode(binaryBitmap)
            onResult(result)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        image.close()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
