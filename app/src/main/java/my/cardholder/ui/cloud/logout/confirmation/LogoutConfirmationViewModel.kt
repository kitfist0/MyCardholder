// package my.cardholder.ui.cloud.logout.confirmation

// import androidx.lifecycle.SavedStateHandle
// import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch
// import my.cardholder.cloud.CloudSignInAssistant
// import my.cardholder.data.SettingsRepository
// import my.cardholder.data.model.CloudProvider
// import my.cardholder.di.Google
// import my.cardholder.di.Yandex
// import my.cardholder.ui.base.BaseViewModel
// import my.cardholder.util.Text
// import javax.inject.Inject

// @HiltViewModel
// class LogoutConfirmationViewModel @Inject constructor(
//     @Google private val googleCloudSignInAssistant: CloudSignInAssistant,
//     @Yandex private val yandexCloudSignInAssistant: CloudSignInAssistant,
//     private val settingsRepository: SettingsRepository,
//     savedStateHandle: SavedStateHandle,
// ) : BaseViewModel() {

//     private val _state = MutableStateFlow(
//         LogoutConfirmationState(
//             accountName = LogoutConfirmationDialogArgs.fromSavedStateHandle(savedStateHandle).accountName
//         )
//     )
//     val state = _state.asStateFlow()

//     fun onLogoutButtonClicked() {
//         viewModelScope.launch {
//             val cloudProvider = settingsRepository.cloudProvider.first()
//             when (cloudProvider) {
//                 CloudProvider.GOOGLE -> googleCloudSignInAssistant.signOut()
//                 CloudProvider.YANDEX -> yandexCloudSignInAssistant.signOut()
//             }
//                 .onSuccess {
//                     settingsRepository.setCloudSyncEnabled(false)
//                     navigate(LogoutConfirmationDialogDirections.fromLogoutConfirmationToSettings())
//                 }
//                 .onFailure {
//                     showToast(Text.Simple("ERROR: ${it.message}"))
//                 }
//         }
//     }
// }
