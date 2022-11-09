package my.cardholder.ui.base

import androidx.navigation.NavDirections
import androidx.navigation.Navigator

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
