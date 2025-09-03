package my.cardholder.ui.cloud.login

import my.cardholder.data.model.CloudProvider

data class CloudItemState(
    val cloudProvider: CloudProvider,
    val isSelected: Boolean,
    val isAvailable: Boolean,
)

sealed class CloudLoginState {
    data class Selection(val cloudItemStates: List<CloudItemState>) : CloudLoginState()
    data object Loading : CloudLoginState()
}
