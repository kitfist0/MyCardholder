package my.cardholder.ui.cloud.logout

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCloudLogoutBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.cloud.login.CloudLoginViewModel
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CloudLogoutFragment : BaseFragment<FragmentCloudLogoutBinding>(
    FragmentCloudLogoutBinding::inflate
) {

    override val viewModel: CloudLoginViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
        }
    }
}
