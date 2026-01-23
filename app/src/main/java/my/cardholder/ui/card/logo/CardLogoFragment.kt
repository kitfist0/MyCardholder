package my.cardholder.ui.card.logo

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardLogoBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.loadLogoImage
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardLogoFragment : BaseFragment<FragmentCardLogoBinding>(
    FragmentCardLogoBinding::inflate
) {

    override val viewModel: CardLogoViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardLogoOkFab.setOnClickListener {
                viewModel.onOkButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.cardLogoWebsiteIconSampleText.editText
                ?.setTextAndSelectionIfRequired(state.websiteIconLink)
            binding.cardLogoWebsiteIconSampleImage.loadLogoImage(
                logoUrl = state.websiteIconLink,
                defaultDrawableRes = R.drawable.ic_broken_img
            )
            binding.cardLogoImageLinkSampleText.editText
                ?.setTextAndSelectionIfRequired(state.imageLogoLink)
            binding.cardLogoImageLinkSampleImage.loadLogoImage(
                logoUrl = state.imageLogoLink,
                defaultDrawableRes = R.drawable.ic_broken_img
            )
        }
    }
}
