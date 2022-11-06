package my.cardholder.ui.scanner.preview

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.cardholder.util.BarcodeAnalyzer
import my.cardholder.data.CardRepository
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.getSupportedFormat
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

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
    ) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
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
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, mainExecutor)
    }

    private fun insertCardAndNavigateToEditor(text: String, supportedFormat: SupportedFormat) {
        viewModelScope.launch {
            cardRepository.insertCard(
                text = text,
                supportedFormat = supportedFormat,
            )
            navigate(ScannerPreviewFragmentDirections.fromPreviewToCardholder())
        }
    }
}
