package my.cardholder.ui.settings.cloud

import my.cardholder.data.model.CloudProvider

sealed class CloudSettingsState {
    data class Disabled(val availableProviders: List<CloudProvider>) : CloudSettingsState()
    data class Enabled(val cloudProvider: CloudProvider, val login: String) : CloudSettingsState()
    data object Loading : CloudSettingsState()
}
