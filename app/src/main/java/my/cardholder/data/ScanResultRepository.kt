package my.cardholder.data

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import my.cardholder.data.model.Card
import my.cardholder.data.model.ScanResult
import my.cardholder.data.model.SupportedFormat
import my.cardholder.util.ext.getAverageColor
import my.cardholder.util.ext.toRotatedBitmap
import my.cardholder.util.findClosestColor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanResultRepository @Inject constructor(
    private val barcodeScanner: BarcodeScanner,
) {

    private val cameraScanResultChannel = Channel<ScanResult>(Channel.RENDEZVOUS)
    val cameraScanResult = cameraScanResultChannel.receiveAsFlow()

    private val fileScanResultChannel = Channel<ScanResult>(Channel.RENDEZVOUS)
    val fileScanResult = fileScanResultChannel.receiveAsFlow()

    @OptIn(ExperimentalGetImage::class)
    fun scan(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val scanResult = barcodes.toScanResult(imageProxy)
                    cameraScanResultChannel.trySend(scanResult)
                }
                .addOnFailureListener { exception ->
                    cameraScanResultChannel.trySend(ScanResult.Failure(exception))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    fun scan(inputImage: InputImage) {
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val scanResult = barcodes.toScanResult()
                fileScanResultChannel.trySend(scanResult)
            }
            .addOnFailureListener { exception ->
                fileScanResultChannel.trySend(ScanResult.Failure(exception))
            }
    }

    private fun List<Barcode>.toScanResult(imageProxy: ImageProxy? = null): ScanResult {
        val barcode = firstOrNull()
        val format = barcode?.getSupportedFormat()
        return when {
            barcode == null || format == null -> ScanResult.Nothing
            else -> ScanResult.Success(
                content = barcode.displayValue.toString(),
                format = format,
                color = imageProxy?.let { detectBackgroundCardColor(it, barcode.boundingBox) },
            )
        }
    }

    /**
     * Determines the dominant background color surrounding the barcode in [imageProxy]
     * (i.e. the color of the physical card the code is printed on), excluding the barcode's own
     * black/white pattern, then matches it to the closest color available for cards.
     */
    private fun detectBackgroundCardColor(imageProxy: ImageProxy, barcodeBoundingBox: Rect?): String? {
        val backgroundArgb = imageProxy.toRotatedBitmap()?.getAverageColor(excludeRect = barcodeBoundingBox)
        return backgroundArgb?.let { findClosestColor(Card.COLORS, it) }
    }

    private fun Barcode.getSupportedFormat(): SupportedFormat? {
        return when (format) {
            Barcode.FORMAT_AZTEC -> SupportedFormat.AZTEC
            Barcode.FORMAT_CODABAR -> SupportedFormat.CODABAR
            Barcode.FORMAT_CODE_39 -> SupportedFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> SupportedFormat.CODE_93
            Barcode.FORMAT_CODE_128 -> SupportedFormat.CODE_128
            Barcode.FORMAT_DATA_MATRIX -> SupportedFormat.DATA_MATRIX
            Barcode.FORMAT_EAN_8 -> SupportedFormat.EAN_8
            Barcode.FORMAT_EAN_13 -> SupportedFormat.EAN_13
            Barcode.FORMAT_ITF -> SupportedFormat.ITF
            Barcode.FORMAT_PDF417 -> SupportedFormat.PDF_417
            Barcode.FORMAT_QR_CODE -> SupportedFormat.QR_CODE
            Barcode.FORMAT_UPC_A -> SupportedFormat.UPC_A
            Barcode.FORMAT_UPC_E -> SupportedFormat.UPC_E
            else -> null
        }
    }
}
