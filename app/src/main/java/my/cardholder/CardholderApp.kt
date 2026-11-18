package my.cardholder

import android.app.Application
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.theme.DynamicColorsManager
import my.cardholder.widget.CardWidgetManager
import javax.inject.Inject

@HiltAndroidApp
class CardholderApp : Application() {

    @Inject
    lateinit var cardShortcutManager: CardShortcutManager

    @Inject
    lateinit var cardWidgetManager: CardWidgetManager

    @Inject
    lateinit var dynamicColorsManager: DynamicColorsManager

    override fun onCreate() {
        super.onCreate()
        dynamicColorsManager.applyToActivities(this)
        WorkManager.getInstance(applicationContext).cancelAllWork()
        cardShortcutManager.observeCardDeletions()
        cardWidgetManager.observeCardChanges()
        cardWidgetManager.observeDynamicColors()
    }
}
