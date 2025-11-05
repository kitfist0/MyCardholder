package my.cardholder.ui.card.backup

import androidx.annotation.StringRes

data class CardBackupState(
    @StringRes val titleRes: Int,
    val progressPercentage: Int?,
    val syncCardsButtonIsVisible: Boolean,
    val launchBackupFileExport: Boolean,
    val launchBackupFileImport: Boolean,
) {
    companion object {
        fun CardBackupState.currentlyInProgress(): Boolean {
            return progressPercentage != null && progressPercentage >= 0
        }
    }
}
