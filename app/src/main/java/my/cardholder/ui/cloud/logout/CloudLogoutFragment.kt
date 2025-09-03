package my.cardholder.ui.cloud.logout

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.CloudProvider.Companion.getDrawableRes
import my.cardholder.databinding.FragmentCloudLogoutBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setStartEndCompoundDrawables
import my.cardholder.util.ext.textToString
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CloudLogoutFragment : BaseFragment<FragmentCloudLogoutBinding>(
    FragmentCloudLogoutBinding::inflate
) {

    override val viewModel: CloudLogoutViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            binding.cloudLogoutFab.setOnClickListener {
                viewModel.onLogoutFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CloudLogoutState.Default -> {
                    enableOrDisableCards(true)
                    binding.cloudLogoutLoadingProgress.isVisible = false

                    binding.cloudLogoutCloudProviderText.apply {
                        text = state.selectedCloudProvider.cloudName
                        setStartEndCompoundDrawables(
                            startDrawableResId = state.selectedCloudProvider.getDrawableRes(),
                            endDrawableResId = R.drawable.ic_done
                        )
                    }
                    binding.cloudLogoutCloudAccountNameText.text = textToString(state.accountNameText)
                }

                is CloudLogoutState.Loading -> {
                    enableOrDisableCards(false)
                    binding.cloudLogoutLoadingProgress.isVisible = true
                }
            }
        }
    }

    private fun enableOrDisableCards(areEnabled: Boolean) {
        val alphaValue = if (areEnabled) 1.0f else 0.5f
        binding.cloudLogoutCloudProviderText.apply {
            alpha = alphaValue
            isEnabled = areEnabled
        }
        binding.cloudLogoutCloudAccountNameText.apply {
            alpha = alphaValue
            isEnabled = areEnabled
        }
    }
}
