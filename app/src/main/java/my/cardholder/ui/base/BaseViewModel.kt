package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel : ViewModel() {

    private val eventChannel = Channel<BaseEvent>()
    val baseEvents = eventChannel.receiveAsFlow()

    protected fun navigate(direction: NavDirections, extras: Navigator.Extras? = null) {
        eventChannel.trySend(Navigate(direction, extras))
    }

    protected fun navigateBack() {
        eventChannel.trySend(NavigateBack)
    }

    protected fun startActivity(action: String, uriString: String? = null) {
        eventChannel.trySend(StartActivity(action, uriString))
    }

    protected fun showSnack(message: String) {
        eventChannel.trySend(SnackMessage(message))
    }
}
