package my.cardholder.ui.card.backup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import my.cardholder.R
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.BackupOperationType
import my.cardholder.data.model.BackupResult
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.ui.card.backup.CardBackupState.Companion.currentlyInProgress
import my.cardholder.util.Text
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class CardBackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        val DEFAULT_STATE = CardBackupState(
            titleRes = R.string.card_backup_dialog_default_title,
            progressPercentage = null,
            syncCardsButtonIsVisible = true,
            launchBackupFileExport = false,
            launchBackupFileImport = false,
        )
    }

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { isEnabled ->
                _state.update { it.copy(syncCardsButtonIsVisible = !isEnabled) }
            }
            .launchIn(viewModelScope)
    }

    private val _state = MutableStateFlow(DEFAULT_STATE)
    val state = _state.asStateFlow()

    fun onDialogDismiss() {
        if (_state.value.currentlyInProgress()) {
            showToast(Text.Resource(R.string.card_backup_dialog_operation_canceled_message))
        }
    }

    fun onExportCardsButtonClicked() {
        _state.update { it.copy(launchBackupFileExport = true) }
    }

    fun onBackupFileExportLaunched() {
        _state.update { it.copy(launchBackupFileExport = false) }
    }

    fun onBackupFileExportResult(outputStream: OutputStream?) {
        outputStream ?: return
        backupRepository.exportToBackupFile(outputStream)
            .onStart {
                _state.update { it.copy(titleRes = R.string.card_backup_dialog_export_title) }
            }
            .onEach { onEachBackupResult(it) }
            .launchIn(viewModelScope)
    }

    fun onImportCardsButtonClicked() {
        _state.update { it.copy(launchBackupFileImport = true) }
    }

    fun onSyncCardsButtonClicked() {
        navigate(CardBackupDialogDirections.fromCardBackupToCloudLogin())
    }

    fun onBackupFileImportLaunched() {
        _state.update { it.copy(launchBackupFileImport = false) }
    }

    fun onBackupFileImportResult(inputStream: InputStream?) {
        inputStream ?: return
        backupRepository.importFromBackupFile(inputStream)
            .onStart {
                _state.update { it.copy(titleRes = R.string.card_backup_dialog_import_title) }
            }
            .onEach { onEachBackupResult(it) }
            .launchIn(viewModelScope)
    }

    private fun onEachBackupResult(result: BackupResult) {
        when (result) {
            is BackupResult.Error -> {
                _state.value = DEFAULT_STATE
                showToast(Text.Simple(result.message))
            }

            is BackupResult.Progress -> {
                _state.update { it.copy(progressPercentage = result.percentage) }
            }

            is BackupResult.Success -> {
                _state.value = DEFAULT_STATE
                showSuccessToast(result.type)
                navigateUp()
            }
        }
    }

    private fun showSuccessToast(type: BackupOperationType) {
        val text = when (type) {
            BackupOperationType.IMPORT ->
                Text.Resource(R.string.card_backup_dialog_import_completed_message)

            BackupOperationType.EXPORT ->
                Text.Resource(R.string.card_backup_dialog_export_completed_message)
        }
        showToast(text)
    }
}
