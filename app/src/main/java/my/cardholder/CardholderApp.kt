package my.cardholder

import android.app.Application
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CardholderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(applicationContext).cancelAllWork()
    }
}
