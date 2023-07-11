package my.cardholder.ui.card.backup

import androidx.annotation.StringRes

data class CardBackupState(
    @StringRes val titleRes: Int,
    val progress: Int?,
    val launchCardsExport: Boolean,
    val launchCardsImport: Boolean,
) {
    companion object {
        fun CardBackupState.currentlyInProgress(): Boolean {
            return progress != null && progress >= 0
        }
    }
}
