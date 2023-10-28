package my.cardholder.util.analyzer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraBarcodeAnalyzer(
    private val barcodeScanner: BarcodeScanner,
) : ImageAnalysis.Analyzer {

    private val _barcode = MutableStateFlow<Barcode?>(null)
    val barcode = _barcode.asStateFlow()

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    _barcode.value = barcodes.firstOrNull()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
