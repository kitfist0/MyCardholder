package my.cardholder.ui.base

import androidx.navigation.NavDirections
import androidx.navigation.Navigator

interface BaseEvent

data class SnackMessage(val message: String) : BaseEvent

data class Navigate(val direction: NavDirections, val extras: Navigator.Extras? = null) : BaseEvent
