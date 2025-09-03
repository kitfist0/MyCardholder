package my.cardholder.ui.cloud.login

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.CloudProvider.Companion.getDrawableRes
import my.cardholder.databinding.FragmentCloudLoginBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setStartEndCompoundDrawables
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CloudLoginFragment : BaseFragment<FragmentCloudLoginBinding>(
    FragmentCloudLoginBinding::inflate
) {

    private val cloudLoginRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        viewModel.onLoginRequestResult(activityResult)
    }

    override val viewModel: CloudLoginViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cloudLoginGoogleCloudCard.setOnClickListener {
                viewModel.onGoogleCloudCardClicked()
            }
            cloudLoginYandexCloudCard.setOnClickListener {
                viewModel.onYandexCloudCardClicked()
            }
            cloudLoginFab.setOnClickListener {
                viewModel.onLoginFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.loginIntents) { intent ->
            cloudLoginRequest.launch(intent)
        }
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CloudLoginState.Loading -> {
                    enableOrDisableCards(false)
                    binding.cloudLoginLoadingProgress.isVisible = true
                }

                is CloudLoginState.Selection -> {
                    enableOrDisableCards(true)
                    binding.cloudLoginLoadingProgress.isVisible = false

                    state.cloudItemStates.forEach { cloudItemState ->
                        val cloudProvider = cloudItemState.cloudProvider
                        if (cloudProvider == CloudProvider.GOOGLE) {
                            binding.cloudLoginGoogleCloudText.text = cloudProvider.cloudName
                            binding.cloudLoginGoogleCloudText.setStartEndCompoundDrawables(
                                startDrawableResId = cloudProvider.getDrawableRes(),
                                endDrawableResId = if (cloudItemState.isSelected) R.drawable.ic_done else null
                            )
                        } else if (cloudProvider == CloudProvider.YANDEX) {
                            binding.cloudLoginYandexCloudText.text = cloudProvider.cloudName
                            binding.cloudLoginYandexCloudText.setStartEndCompoundDrawables(
                                startDrawableResId = cloudProvider.getDrawableRes(),
                                endDrawableResId = if (cloudItemState.isSelected) R.drawable.ic_done else null
                            )
                        }
                    }
                }
            }
        }
    }

    private fun enableOrDisableCards(areEnabled: Boolean) {
        val alphaValue = if (areEnabled) 1.0f else 0.5f
        binding.cloudLoginGoogleCloudCard.apply {
            alpha = alphaValue
            isEnabled = areEnabled
        }
        binding.cloudLoginYandexCloudCard.apply {
            alpha = alphaValue
            isEnabled = areEnabled
        }
    }
}
