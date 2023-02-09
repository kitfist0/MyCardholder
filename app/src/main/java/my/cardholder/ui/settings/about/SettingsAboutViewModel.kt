package my.cardholder.ui.settings.about

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.BuildConfig
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsAboutViewModel @Inject constructor() : BaseViewModel() {

    private companion object {
        const val ACTION_VIEW = "android.intent.action.VIEW"
    }

    private val _dialogTitle = MutableStateFlow(
        "${BuildConfig.APP_NAME} ${BuildConfig.VERSION_NAME}\n${BuildConfig.DEV_NAME}, 2023"
    )
    val dialogTitle = _dialogTitle.asStateFlow()

    fun onPrivacyPolicyButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_POLICY)
    }

    fun onSourceCodeButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_GITHUB)
    }

    fun onCopyrightButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_LICENSE)
    }
}
