package my.cardholder.di

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.CameraBarcodeAnalyzer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    @Provides
    fun provideCameraProviderFuture(context: Context): ListenableFuture<ProcessCameraProvider> {
        return ProcessCameraProvider.getInstance(context)
    }

    @Provides
    fun provideMainExecutor(context: Context): Executor {
        return ContextCompat.getMainExecutor(context)
    }

    @Provides
    @Singleton
    fun provideBarcodeScanner(): BarcodeScanner {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        return BarcodeScanning.getClient(options)
    }

    @Provides
    @Singleton
    fun provideCameraBarcodeAnalyzer(barcodeScanner: BarcodeScanner): CameraBarcodeAnalyzer {
        return CameraBarcodeAnalyzer(barcodeScanner)
    }

    @Provides
    fun provideImageAnalysis(cameraBarcodeAnalyzer: CameraBarcodeAnalyzer): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), cameraBarcodeAnalyzer)
        return imageAnalysis
    }
}
