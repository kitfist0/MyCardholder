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
import my.cardholder.data.model.BackupResult
import my.cardholder.ui.base.BaseViewModel
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class CardBackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : BaseViewModel() {

    private companion object {
        val DEFAULT_STATE = CardBackupState(
            titleRes = R.string.card_backup_dialog_default_title,
            progressPercentage = null,
            launchBackupFileExport = false,
            launchBackupFileImport = false,
        )
    }

    private val _state = MutableStateFlow(DEFAULT_STATE)
    val state = _state.asStateFlow()

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
                showSnack(result.message)
            }
            is BackupResult.Progress -> {
                _state.update { it.copy(progressPercentage = result.percentage) }
            }
            is BackupResult.Success -> {
                _state.value = DEFAULT_STATE
                navigateUp()
            }
        }
    }
}
