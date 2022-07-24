package my.cardholder.ui.scanner

import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import my.cardholder.AppExecutors
import my.cardholder.analyzer.QrCodeAnalyzer
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val appExecutors: AppExecutors,
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
) : ViewModel() {

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        targetSize: Size,
    ) {
        cameraProviderFuture.addListener({
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val preview = Preview.Builder()
                .build()
            preview.setSurfaceProvider(surfaceProvider)
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(targetSize)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(appExecutors.analysisExecutor(), QrCodeAnalyzer { qrResult ->
                Log.d("SCANNER_VIEW_MODEL", "qrResult: ${qrResult.text}")
            })
            cameraProviderFuture.get()
                .bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, appExecutors.mainExecutor())
    }
}
