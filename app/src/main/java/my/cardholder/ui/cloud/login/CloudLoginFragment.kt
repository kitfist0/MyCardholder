package my.cardholder.ui.cloud.login

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCloudLoginBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CloudLoginFragment : BaseFragment<FragmentCloudLoginBinding>(
    FragmentCloudLoginBinding::inflate
) {

    private val cloudSignInRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        viewModel.onGoogleCloudSignInRequestResult(activityResult)
    }

    override val viewModel: CloudLoginViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            viewModel.onCloudSignInRequestLaunched()
        }
    }
}
