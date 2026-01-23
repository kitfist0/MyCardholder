package my.cardholder.ui.card.logo

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.BuildConfig
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardLogoViewModel @Inject constructor() : BaseViewModel() {

    // private val defaultState = CardLogoState(
    //     websiteIconLink = BuildConfig.WEB_PAGE_STORE,
    //     imageLogoLink = BuildConfig.APP_LOGO_LINK,
    // // )
    // val state = MutableStateFlow(defaultState).asStateFlow()

    fun onOkButtonClicked() {
        navigateUp()
    }
}
