package my.cardholder.ui.scanner.preview

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.cardholder.util.BarcodeAnalyzer
import my.cardholder.data.CardRepository
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ScannerPreviewViewModel @Inject constructor(
    private val mainExecutor: Executor,
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
    ) {
        val cameraProvider = cameraProviderFuture.await()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(surfaceProvider)
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            BarcodeAnalyzer { barcode ->
                barcode.getSupportedFormat()?.let { supportedFormat ->
                    mainExecutor.execute {
                        imageAnalysis.clearAnalyzer()
                        cameraProvider.unbindAll()
                        insertCardAndNavigateToEditor(
                            text = barcode.displayValue.toString(),
                            supportedFormat = supportedFormat,
                        )
                    }
                }
            }
        )
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
    }

    private fun insertCardAndNavigateToEditor(text: String, supportedFormat: SupportedFormat) {
        viewModelScope.launch {
            cardRepository.insertCard(text, supportedFormat)
            navigate(ScannerPreviewFragmentDirections.fromPreviewToCardholder())
        }
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
