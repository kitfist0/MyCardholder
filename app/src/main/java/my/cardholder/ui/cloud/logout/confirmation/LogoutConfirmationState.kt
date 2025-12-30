package my.cardholder.ui.cloud.logout.confirmation

import my.cardholder.R
import my.cardholder.util.Text

data class LogoutConfirmationState(
    val accountName: String,
) {
    companion object {
        fun LogoutConfirmationState.getTitleText(): Text {
            return Text.ResourceAndParams(R.string.cloud_logout_confirmation_title_text, listOf(accountName))
        }
    }
}
