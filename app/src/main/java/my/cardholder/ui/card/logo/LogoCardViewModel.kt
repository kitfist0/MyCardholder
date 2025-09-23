package my.cardholder.ui.card.logo

import dagger.hilt.android.lifecycle.HiltViewModel
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class LogoCardViewModel @Inject constructor() : BaseViewModel() {
    fun onOkButtonClicked() {
        navigateUp()
    }
}
