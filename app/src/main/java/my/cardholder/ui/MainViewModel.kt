package my.cardholder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.cloud.BackupChecksum
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.SyncRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.usecase.CloudSyncUseCase
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Result
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val cloudSyncUseCase: CloudSyncUseCase,
    purchasedProductsProvider: PurchasedProductsProvider,
    private val coffeeRepository: CoffeeRepository,
    private val networkChecker: NetworkChecker,
    private val settingsRepository: SettingsRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    companion object {
        const val LOCAL_CHANGES_DEBOUNCE_MS = 2_000L
    }

    val appTheme = settingsRepository.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppTheme.SYSTEM
    )

    // Unlimited, so that the sync is never suspended while the UI is not collecting the log
    private val backupDownloadLogChannel = Channel<String?>(Channel.UNLIMITED)
    val backupDownloadLog = backupDownloadLogChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            purchasedProductsProvider.purchasedProducts.collect { purchasedIds ->
                coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
            }
        }

        settingsRepository.cloudSyncEnabled
            .distinctUntilChanged()
            .flatMapLatest { syncEnabled ->
                if (syncEnabled) syncOnLocalChanges() else emptyFlow()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Syncs immediately when sync starts, then after each local change.
     * Syncs never overlap: changes made during a sync, including the merge itself, are handled after it.
     */
    private fun syncOnLocalChanges(): Flow<Result<BackupChecksum>> {
        return syncRepository.localChecksum
            .withIndex()
            .debounce { (index, _) -> if (index == 0) 0L else LOCAL_CHANGES_DEBOUNCE_MS }
            .conflate()
            .filter { (index, checksum) ->
                val isInitialSync = index == 0
                val hasUnsyncedChanges = checksum != settingsRepository.latestSyncedBackupChecksum.first()
                (isInitialSync || hasUnsyncedChanges) && networkChecker.isNetworkAvailable()
            }
            .flatMapConcat { (index, _) ->
                val isInitialSync = index == 0
                cloudSyncUseCase.execute(settingsRepository.cloudProvider.first())
                    .onEach { result -> if (isInitialSync) logSyncResult(result) }
            }
    }

    private fun logSyncResult(result: Result<BackupChecksum>) {
        val message = when (result) {
            is Result.Error -> "Sync error: ${result.throwable.message}"
            is Result.Loading -> result.message
            is Result.Success -> "Sync completed"
        }
        backupDownloadLogChannel.trySend(message)
        if (result !is Result.Loading) {
            backupDownloadLogChannel.trySend(null)
        }
    }
}
