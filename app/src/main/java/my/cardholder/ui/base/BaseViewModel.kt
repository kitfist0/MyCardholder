package my.cardholder.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import my.cardholder.util.Text

sealed interface BaseEvent {
    data class Navigate(
        val direction: NavDirections,
        val extras: Navigator.Extras? = null,
    ) : BaseEvent

    data object NavigateUp : BaseEvent

    data class SnackMessage(
        val text: Text,
    ) : BaseEvent

    data class ShowOkSnack(
        val actionCode: Int,
        val text: Text,
    ) : BaseEvent

    sealed class StartActivity() : BaseEvent {
        data class ActionView(val uriString: String): StartActivity()
        data class ActionSend(val extraText: String): StartActivity()
        data class AppDetails(val packageName: String): StartActivity()
    }

    data class ToastMessage(
        val text: Text,
    ) : BaseEvent
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

    protected fun showOkSnack(actionCode: Int, text: Text) {
        sendEvent(BaseEvent.ShowOkSnack(actionCode, text))
    }

    open fun onOkSnackButtonClicked(actionCode: Int) {
    }

    protected fun startActivityToOpenAppDetails(packageName: String) {
        sendEvent(BaseEvent.StartActivity.AppDetails(packageName))
    }

    protected fun startActivityToOpenWebPage(uriString: String) {
        sendEvent(BaseEvent.StartActivity.ActionView(uriString))
    }

    protected fun startActivityToShare(extraText: String) {
        sendEvent(BaseEvent.StartActivity.ActionSend(extraText))
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
