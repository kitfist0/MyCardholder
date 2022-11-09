package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
