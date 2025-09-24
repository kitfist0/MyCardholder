package my.cardholder.ui.card.logo

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardLogoViewModel @Inject constructor() : BaseViewModel() {

    private companion object {
        const val IMAGE_LINK = "https://play-lh.googleusercontent.com/CukdHWulsmo9nwmiLmJg9tViyUv2UfWV5KUMw-Emm-wnAmKfjItajKidbdRw0MR8i6o=w480-h960-rw"
        const val WEBSITE_LINK = "https://play.google.com"
    }

    private val defaultState = CardLogoState(
        websiteIconLink = WEBSITE_LINK,
        imageLogoLink = IMAGE_LINK,
    )
    val state = MutableStateFlow(defaultState).asStateFlow()

    fun onOkButtonClicked() {
        navigateUp()
    }
}
