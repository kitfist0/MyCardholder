package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class BaseEvent {
    data class Navigate(
        val direction: NavDirections,
        val extras: Navigator.Extras? = null,
    ) : BaseEvent()

    object NavigateUp : BaseEvent()

    data class SnackMessage(val message: String) : BaseEvent()

    data class StartActivity(
        val action: String,
        val uriString: String? = null,
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

    protected fun showSnack(message: String) {
        sendEvent(BaseEvent.SnackMessage(message))
    }

    protected fun startActivity(action: String, uriString: String? = null) {
        sendEvent(BaseEvent.StartActivity(action, uriString))
    }

    private fun sendEvent(event: BaseEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
