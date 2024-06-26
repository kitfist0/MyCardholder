package my.cardholder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.data.CardRepository
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.usecase.CloudDownloadUseCase
import my.cardholder.usecase.CloudUploadUseCase
import my.cardholder.util.Result
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    cardRepository: CardRepository,
    cloudDownloadUseCase: CloudDownloadUseCase,
    cloudUploadUseCase: CloudUploadUseCase,
    purchasedProductsProvider: PurchasedProductsProvider,
    settingsRepository: SettingsRepository,
    private val coffeeRepository: CoffeeRepository,
) : ViewModel() {

    val nightModeEnabled = settingsRepository.nightModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val backupDownloadResultChannel = Channel<Result<String>>()
    val backupDownloadResult = backupDownloadResultChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            purchasedProductsProvider.purchasedProducts.collect { purchasedIds ->
                coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
            }
        }

        cloudDownloadUseCase.execute()
            .onEach { result -> backupDownloadResultChannel.send(result) }
            .launchIn(viewModelScope)

        cardRepository.checksumOfAllCards
            .flatMapLatest { checksum -> cloudUploadUseCase.execute(checksum) }
            .launchIn(viewModelScope)
    }
}
