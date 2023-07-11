package my.cardholder.ui.card.backup

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCardBackupBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.card.backup.CardBackupState.Companion.currentlyInProgress
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardBackupDialog : BaseDialogFragment<DialogCardBackupBinding>(
    DialogCardBackupBinding::inflate
) {

    private companion object {
        const val EXPORTED_FILE_NAME = "exported.csv"
        const val MIME_TYPE = "*/*"
    }

    private val exportCards =
        registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
            val outputStream = uri?.let { requireActivity().contentResolver.openOutputStream(it) }
            viewModel.onExportCardsResult(outputStream)
        }

    private val importCards =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val inputStream = uri?.let { requireActivity().contentResolver.openInputStream(it) }
            viewModel.onImportCardsResult(inputStream)
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
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            val notCurrentlyInProgress = !state.currentlyInProgress()
            binding.cardBackupTitleText.setText(state.titleRes)
            binding.cardBackupProgressIndicator.apply {
                progress = state.progress ?: 0
                isInvisible = notCurrentlyInProgress
            }
            binding.cardBackupExportCardsButton.isEnabled = notCurrentlyInProgress
            binding.cardBackupImportCardsButton.isEnabled = notCurrentlyInProgress
            if (state.launchCardsExport) {
                exportCards.launch(EXPORTED_FILE_NAME)
                viewModel.onExportCardsLaunched()
            } else if (state.launchCardsImport) {
                importCards.launch(MIME_TYPE)
                viewModel.onImportCardsLaunched()
            }
        }
    }
}
