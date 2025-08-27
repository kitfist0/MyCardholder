package my.cardholder.ui.settings.cloud

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentSettingsCloudBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CloudSettingsFragment : BaseFragment<FragmentSettingsCloudBinding>(
    FragmentSettingsCloudBinding::inflate
) {

    private val googleCloudSignInRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        viewModel.onGoogleCloudSignInRequestResult(activityResult)
    }

    override val viewModel: CloudSettingsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            viewModel.onGoogleCloudSignInRequestLaunched()
        }
    }
}
