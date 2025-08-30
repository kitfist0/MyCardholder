package my.cardholder.ui.cloud.logout

import my.cardholder.data.model.CloudProvider
import my.cardholder.util.Text

sealed class CloudLogoutState {
    data class Info(
        val selectedCloudProvider: CloudProvider,
        val accountNameText: Text,
    ) : CloudLogoutState()

    data object Loading : CloudLogoutState()
}
