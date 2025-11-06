package my.cardholder.ui.card.backup

import android.content.DialogInterface
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.BuildConfig
import my.cardholder.databinding.DialogCardBackupBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.card.backup.CardBackupState.Companion.currentlyInProgress
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardBackupDialog : BaseDialogFragment<DialogCardBackupBinding>(
    DialogCardBackupBinding::inflate
) {

    private companion object {
        const val EXPORTED_FILE_NAME = BuildConfig.APP_NAME + "Backup.csv"
        const val MIME_TYPE = "*/*"
    }

    private val backupFileExport =
        registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
            val outputStream = uri?.let { requireActivity().contentResolver.openOutputStream(it) }
            viewModel.onBackupFileExportResult(outputStream)
        }

    private val backupFileImport =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val inputStream = uri?.let { requireActivity().contentResolver.openInputStream(it) }
            viewModel.onBackupFileImportResult(inputStream)
        }

    override val viewModel: CardBackupViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            cardBackupExportCardsButton.setOnClickListener {
                viewModel.onExportCardsButtonClicked()
            }
            cardBackupImportCardsButton.setOnClickListener {
                viewModel.onImportCardsButtonClicked()
            }
            cardBackupSyncCardsButton.setOnClickListener {
                viewModel.onSyncCardsButtonClicked()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDialogDismiss()
        super.onDismiss(dialog)
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            updateViews(state)
            if (state.launchBackupFileExport) {
                backupFileExport.launch(EXPORTED_FILE_NAME)
                viewModel.onBackupFileExportLaunched()
            } else if (state.launchBackupFileImport) {
                backupFileImport.launch(MIME_TYPE)
                viewModel.onBackupFileImportLaunched()
            }
        }
    }

    private fun updateViews(state: CardBackupState) {
        val notCurrentlyInProgress = !state.currentlyInProgress()
        with(binding) {
            cardBackupTitleText.setText(state.titleRes)
            cardBackupProgressIndicator.apply {
                progress = state.progressPercentage ?: 0
                isInvisible = notCurrentlyInProgress
            }
            cardBackupExportCardsButton.apply {
                isVisible = state.exportCardsButtonIsVisible
                isEnabled = notCurrentlyInProgress
            }
            cardBackupImportCardsButton.apply {
                isVisible = state.importCardsButtonIsVisible
                isEnabled = notCurrentlyInProgress
            }
            cardBackupSyncCardsButton.apply {
                isVisible = state.syncCardsButtonIsVisible
                isEnabled = notCurrentlyInProgress
            }
        }
    }
}
