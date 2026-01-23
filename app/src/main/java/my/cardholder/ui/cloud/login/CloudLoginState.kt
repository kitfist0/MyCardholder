// package my.cardholder.ui.cloud.login

// import my.cardholder.data.model.CloudProvider

// data class CloudProviderState(
//     val cloudProvider: CloudProvider,
//     val isSelected: Boolean,
//     val isAvailable: Boolean,
// )

// sealed class CloudLoginState {
//     data class Selection(val cloudProviderStates: List<CloudProviderState>) : CloudLoginState()
//     data object Loading : CloudLoginState()
// }
