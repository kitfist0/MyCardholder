package my.cardholder.di

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor

@Module
@InstallIn(SingletonComponent::class)
class CameraModule {

    @Provides
    fun provideCameraProviderFuture(context: Context): ListenableFuture<ProcessCameraProvider> {
        return ProcessCameraProvider.getInstance(context)
    }

    @Provides
    fun provideMainExecutor(context: Context): Executor {
        return ContextCompat.getMainExecutor(context)
    }
}
