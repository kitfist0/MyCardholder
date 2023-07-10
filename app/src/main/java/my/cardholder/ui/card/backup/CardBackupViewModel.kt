package my.cardholder.ui.card.backup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val defaultState = CardBackupState(
        titleRes = R.string.card_backup_dialog_default_title,
        progress = 0,
        launchCardsExport = false,
        launchCardsImport = false,
    )

    private val _state = MutableStateFlow(defaultState)
    val state = _state.asStateFlow()

    fun onExportCardsButtonClicked() {
        _state.update { it.copy(launchCardsExport = true) }
    }

    fun onExportCardsLaunched() {
        _state.update { it.copy(launchCardsExport = false) }
    }

    fun onExportCardsResult(outputStream: OutputStream?) {
        outputStream ?: return
        backupRepository.exportToBackupFile(outputStream)
            .onEach { onEachBackupResult(it) }
            .launchIn(viewModelScope)
    }

    fun onImportCardsButtonClicked() {
        _state.update { it.copy(launchCardsImport = true) }
    }

    fun onImportCardsLaunched() {
        _state.update { it.copy(launchCardsImport = false) }
    }

    fun onImportCardsResult(inputStream: InputStream?) {
        inputStream ?: return
        backupRepository.importFromBackupFile(inputStream)
            .onEach { onEachBackupResult(it) }
            .launchIn(viewModelScope)
    }

    private fun onEachBackupResult(result: BackupResult) {
        when (result) {
            is BackupResult.Error -> {
                _state.value = defaultState
                showSnack(result.message)
            }
            is BackupResult.Progress -> {
                _state.update { it.copy(progress = result.percentage) }
            }
            BackupResult.Success -> {
                _state.value = defaultState
                navigateUp()
            }
        }
    }
}
