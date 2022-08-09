package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel : ViewModel() {

    private val eventChannel = Channel<BaseEvent>(Channel.BUFFERED)
    val baseEvents = eventChannel.receiveAsFlow()

    fun navigate(direction: NavDirections, extras: Navigator.Extras? = null) {
        eventChannel.trySend(Navigate(direction, extras))
    }

    fun showSnack(message: String) {
        eventChannel.trySend(SnackMessage(message))
    }
}
