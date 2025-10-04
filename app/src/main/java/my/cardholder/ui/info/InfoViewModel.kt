package my.cardholder.ui.info

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.BuildConfig
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor() : BaseViewModel() {

    private companion object {
        const val ACTION_VIEW = "android.intent.action.VIEW"
    }

    private val _dialogTitle = MutableStateFlow(
        "${BuildConfig.APP_NAME} ${BuildConfig.VERSION_NAME}\n${BuildConfig.DEV_NAME}, ${BuildConfig.YEAR}"
    )
    val dialogTitle = _dialogTitle.asStateFlow()

    fun onSupportedFormatsButtonClicked() {
        navigate(InfoDialogDirections.fromInfoToSpecs())
    }

    fun onPrivacyPolicyButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_POLICY)
    }

    fun onSourceCodeButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_REPO)
    }

    fun onCopyrightButtonClicked() {
        startActivity(ACTION_VIEW, BuildConfig.WEB_PAGE_LICENSE)
    }
}
