package my.cardholder.theme

import android.app.Activity
import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import my.cardholder.data.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Colors from the wallpaper (Material You), available since Android 12 */
@Singleton
class DynamicColorsManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isEnabled = MutableStateFlow(false)

    /** Activities are themed with the value they are created with, so they are recreated when it changes */
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    val isAvailable: Boolean
        get() = DynamicColors.isDynamicColorAvailable()

    fun applyToActivities(application: Application) {
        // Activities are themed before they are created, so the setting is needed right away.
        // Reading the small settings file once at app start is fast.
        _isEnabled.value = runBlocking { settingsRepository.dynamicColorsEnabled.first() }
        settingsRepository.dynamicColorsEnabled
            .onEach { isEnabled -> _isEnabled.value = isEnabled }
            .launchIn(scope)
        DynamicColors.applyToActivitiesIfAvailable(application, options)
    }

    /**
     * For an activity that sets its theme itself after it's created, e.g. when its splash screen ends.
     * Setting a theme replaces the colors applied before.
     */
    fun applyToActivity(activity: Activity) {
        DynamicColors.applyToActivityIfAvailable(activity, options)
    }

    private val options: DynamicColorsOptions
        get() = DynamicColorsOptions.Builder()
            .setPrecondition { _, _ -> _isEnabled.value }
            .build()
}
