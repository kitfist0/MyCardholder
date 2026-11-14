package my.cardholder

import android.app.Application
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.widget.CardWidgetManager
import javax.inject.Inject

@HiltAndroidApp
class CardholderApp : Application() {

    @Inject
    lateinit var cardShortcutManager: CardShortcutManager

    @Inject
    lateinit var cardWidgetManager: CardWidgetManager

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(applicationContext).cancelAllWork()
        cardShortcutManager.observeCardDeletions()
        cardWidgetManager.observeCardChanges()
    }
}
