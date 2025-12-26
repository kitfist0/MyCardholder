package my.cardholder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.cloud.BackupChecksum
import my.cardholder.data.CardRepository
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.usecase.CloudDownloadUseCase
import my.cardholder.usecase.CloudUploadUseCase
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Result
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    cardRepository: CardRepository,
    cloudDownloadUseCase: CloudDownloadUseCase,
    cloudUploadUseCase: CloudUploadUseCase,
    purchasedProductsProvider: PurchasedProductsProvider,
    private val coffeeRepository: CoffeeRepository,
    private val networkChecker: NetworkChecker,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val appTheme = settingsRepository.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppTheme.SYSTEM
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
                if (syncEnabled && networkChecker.isNetworkAvailable()) {
                    cloudDownloadUseCase.execute(
                        cloudProvider = settingsRepository.cloudProvider.first(),
                        checksum = settingsRepository.latestSyncedBackupChecksum.first() ?: 0L,
                    )
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
            .combine(cardRepository.checksumOfAllCards) { syncEnabled, checksum -> syncEnabled to checksum }
                .distinctUntilChanged()
                .flatMapLatest { (syncEnabled, checksum) ->
                    if (syncEnabled && networkChecker.isNetworkAvailable()) {
                        cloudUploadUseCase.execute(settingsRepository.cloudProvider.first(), checksum)
                    } else {
                        emptyFlow()
                    }
                }
                .onEach { result -> updateLatestSyncedBackupChecksum(result) }
                .launchIn(viewModelScope)
    }

    private suspend fun updateLatestSyncedBackupChecksum(result: Result<BackupChecksum>) {
        if (result is Result.Success) {
            settingsRepository.setLatestSyncedBackupChecksum(result.data)
        }
    }
}
