package my.cardholder.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.Channel

class BarcodeAnalyzer(
    private val context: Context,
    private val barcodeScanner: BarcodeScanner,
) : ImageAnalysis.Analyzer {

    val barcodeChannel = Channel<Barcode?>(Channel.RENDEZVOUS)

    fun analyze(uri: Uri) {
        val inputImage = InputImage.fromFilePath(context, uri)
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodeChannel.trySend(barcodes.firstOrNull())
            }
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodeChannel.trySend(barcodes.firstOrNull())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
