package my.cardholder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import my.cardholder.data.SettingsRepository
import javax.inject.Inject

@HiltAndroidApp
class CardholderApp : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository.setDefaultNightMode()
    }
}
