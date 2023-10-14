package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import my.cardholder.util.Text

sealed class BaseEvent {
    data class Navigate(
        val direction: NavDirections,
        val extras: Navigator.Extras? = null,
    ) : BaseEvent()

    data object NavigateUp : BaseEvent()

    data class SnackMessage(
        val text: Text,
    ) : BaseEvent()

    data class StartActivity(
        val action: String,
        val uriString: String? = null,
    ) : BaseEvent()

    data class ToastMessage(
        val text: Text,
    ) : BaseEvent()
}

abstract class BaseViewModel : ViewModel() {

    private val eventChannel = Channel<BaseEvent>()
    val baseEvents = eventChannel.receiveAsFlow()

    protected fun navigate(direction: NavDirections, extras: Navigator.Extras? = null) {
        sendEvent(BaseEvent.Navigate(direction, extras))
    }

    protected fun navigateUp() {
        sendEvent(BaseEvent.NavigateUp)
    }

    protected fun showSnack(text: Text) {
        sendEvent(BaseEvent.SnackMessage(text))
    }

    protected fun startActivity(action: String, uriString: String? = null) {
        sendEvent(BaseEvent.StartActivity(action, uriString))
    }

    protected fun showToast(text: Text) {
        sendEvent(BaseEvent.ToastMessage(text))
    }

    private fun sendEvent(event: BaseEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
