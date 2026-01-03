package my.cardholder.ui.cloud.logout.confirmation

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.DialogLogoutConfirmationBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class LogoutConfirmationDialog : BaseDialogFragment<DialogLogoutConfirmationBinding>(
    DialogLogoutConfirmationBinding::inflate
) {

    override val viewModel: LogoutConfirmationViewModel by viewModels()

    override fun initViews() {
        binding.logoutConfirmationButton.setOnClickListener {
            viewModel.onLogoutButtonClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.logoutConfirmationTitleText.setSpannableText(state.accountName)
        }
    }

    private fun TextView.setSpannableText(accountName: String) {
        val textWithAccountName =
            context.getString(R.string.cloud_logout_confirmation_title_text)
                .format(accountName)
        val spannableString = SpannableString(textWithAccountName)
        val start = textWithAccountName.indexOf(accountName)
        val end = start + accountName.length
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannableString
    }
}
