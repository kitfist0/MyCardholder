package my.cardholder.ui.base

import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel : ViewModel() {

    private val eventChannel = Channel<BaseEvent>(Channel.BUFFERED)
    val baseEvents = eventChannel.receiveAsFlow()

    open fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    protected fun navigate(direction: NavDirections, extras: Navigator.Extras? = null) {
        eventChannel.trySend(Navigate(direction, extras))
    }

    protected fun navigateBack() {
        eventChannel.trySend(NavigateBack)
    }

    protected fun showSnack(message: String) {
        eventChannel.trySend(SnackMessage(message))
    }
}
