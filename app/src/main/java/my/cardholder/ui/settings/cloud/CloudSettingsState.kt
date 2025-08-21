package my.cardholder.ui.settings.cloud

data class CloudSettingsState(
    val cloudSyncIsEnabled: Boolean,
    val googleCloudProviderIsChecked: Boolean,
    val yandexCloudProviderIsChecked: Boolean,
)
