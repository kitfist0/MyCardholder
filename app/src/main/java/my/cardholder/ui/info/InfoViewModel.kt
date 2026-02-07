package my.cardholder.ui.info

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.BuildConfig
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor() : BaseViewModel() {

    private val _dialogTitle = MutableStateFlow(
        "${BuildConfig.APP_NAME} ${BuildConfig.VERSION_NAME}\n${BuildConfig.APP_STORE_NAME}, ${BuildConfig.DEV_NAME}, ${BuildConfig.YEAR}"
    )
    val dialogTitle = _dialogTitle.asStateFlow()

    fun onSupportedFormatsButtonClicked() {
        navigate(InfoDialogDirections.fromInfoToSpecs())
    }

    fun onPrivacyPolicyButtonClicked() {
        startActivityToOpenWebPage(BuildConfig.WEB_PAGE_POLICY)
    }

    fun onSourceCodeButtonClicked() {
        startActivityToOpenWebPage(BuildConfig.WEB_PAGE_REPO)
    }

    fun onCopyrightButtonClicked() {
        startActivityToOpenWebPage(BuildConfig.WEB_PAGE_LICENSE)
    }
}
