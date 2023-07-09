package my.cardholder.ui.card.backup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.BackupRepository
import my.cardholder.ui.base.BaseViewModel
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class CardBackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CardBackupState(
            titleRes = R.string.card_backup_dialog_default_title,
            progress = 0,
            launchCardsExport = false,
            launchCardsImport = false,
        )
    )
    val state = _state.asStateFlow()

    fun onExportCardsButtonClicked() {
        _state.update { it.copy(launchCardsExport = true) }
    }

    fun onExportCardsLaunched() {
        _state.update { it.copy(launchCardsExport = false) }
    }

    fun onExportCardsResult(outputStream: OutputStream?) {
        outputStream?.let {
            viewModelScope.launch {
                backupRepository.exportCards(it)
                    .onSuccess { showSnack("Export completed") }
                    .onFailure { showSnack(it.message.orEmpty()) }
            }
        }
    }

    fun onImportCardsButtonClicked() {
        _state.update { it.copy(launchCardsImport = true) }
    }

    fun onImportCardsLaunched() {
        _state.update { it.copy(launchCardsImport = false) }
    }

    fun onImportCardsResult(inputStream: InputStream?) {
        inputStream?.let {
            viewModelScope.launch {
                backupRepository.importCards(it)
                    .onSuccess { showSnack("Import completed") }
                    .onFailure { showSnack(it.message.orEmpty()) }
            }
        }
    }
}
