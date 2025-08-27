package my.cardholder.ui.cloud.login

import my.cardholder.data.model.CloudProvider

sealed class CloudLoginState {
    data class Selection(val selectedCloudProvider: CloudProvider) : CloudLoginState()
    data object Loading : CloudLoginState()
}
