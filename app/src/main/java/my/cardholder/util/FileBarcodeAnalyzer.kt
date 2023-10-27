package my.cardholder.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.io.File

class FileBarcodeAnalyzer(
    private val context: Context,
    private val barcodeScanner: BarcodeScanner,
) {

    private val _barcode = MutableStateFlow<Barcode?>(null)
    val barcode = _barcode.asStateFlow().filterNotNull()

    fun analyze(file: File) {
        val inputImage = InputImage.fromFilePath(context, Uri.fromFile(file))
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    _barcode.value = barcodes.first()
                }
            }
    }
}
