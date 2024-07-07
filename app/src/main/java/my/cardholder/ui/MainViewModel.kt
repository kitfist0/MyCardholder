package my.cardholder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.cloud.backup.BackupChecksum
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
    private val coffeeRepository: CoffeeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val nightModeEnabled = settingsRepository.nightModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val backupDownloadLogChannel = Channel<String?>()
    val backupDownloadLog = backupDownloadLogChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            purchasedProductsProvider.purchasedProducts.collect { purchasedIds ->
                coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
            }
        }

        settingsRepository.cloudSyncEnabled
            .flatMapLatest { syncEnabled ->
                if (syncEnabled) {
                    val checksum = settingsRepository.latestSyncedBackupChecksum.first() ?: 0L
                    cloudDownloadUseCase.execute(checksum)
                } else {
                    emptyFlow()
                }
            }
            .onEach { result ->
                updateLatestSyncedBackupChecksum(result)
                when (result) {
                    is Result.Error ->
                        backupDownloadLogChannel.send("Download error: ${result.throwable.message}")
                    is Result.Loading ->
                        backupDownloadLogChannel.send(result.message)
                    is Result.Success ->
                        backupDownloadLogChannel.send("Download completed")
                }
                if (result !is Result.Loading) {
                    backupDownloadLogChannel.send(null)
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.cloudSyncEnabled
            .flatMapLatest { syncEnabled ->
                if (syncEnabled) {
                    val checksum = cardRepository.checksumOfAllCards.first()
                    cloudUploadUseCase.execute(checksum)
                } else {
                    emptyFlow()
                }
            }
            .onEach { result ->
                updateLatestSyncedBackupChecksum(result)
            }
            .launchIn(viewModelScope)

        cardRepository.checksumOfAllCards
            .flatMapLatest { checksum ->
                val syncEnabled = settingsRepository.cloudSyncEnabled.first()
                if (syncEnabled) cloudUploadUseCase.execute(checksum) else emptyFlow()
            }
            .onEach { result ->
                updateLatestSyncedBackupChecksum(result)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateLatestSyncedBackupChecksum(result: Result<BackupChecksum>) {
        if (result is Result.Success) {
            settingsRepository.setLatestSyncedBackupChecksum(result.data)
        }
    }
}
