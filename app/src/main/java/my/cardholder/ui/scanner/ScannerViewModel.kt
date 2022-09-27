package my.cardholder.ui.scanner

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
import my.cardholder.data.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.BarcodeResult
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
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
                BarcodeAnalyzer { barcodeResult ->
                    mainExecutor.execute {
                        imageAnalysis.clearAnalyzer()
                        cameraProvider.unbindAll()
                        onBarcodeResult(barcodeResult)
                    }
                }
            )
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, mainExecutor)
    }

    private fun onBarcodeResult(barcodeResult: BarcodeResult) {
        runCatching { SupportedFormat.valueOf(barcodeResult.codeFormat) }
            .onSuccess { supportedFormat ->
                viewModelScope.launch {
                    val cardId = cardRepository.insertCard(
                        text = barcodeResult.codeData,
                        timestamp = System.currentTimeMillis(),
                        supportedFormat = supportedFormat,
                    )
                    navigate(ScannerFragmentDirections.fromScannerToCardEditor(cardId))
                }
            }
            .onFailure {
                showSnack("Barcode format not supported!")
            }
    }
}
