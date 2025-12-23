package my.cardholder.ui.cloud.login

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCloudLoginBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.makeItFiftyPercentMoreTransparentIf
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

    private val listAdapter = CloudProviderAdapter {
        viewModel.onCloudProviderClicked(it)
    }

    override val viewModel: CloudLoginViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            binding.cloudLoginRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
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
                is CloudLoginState.Loading -> changeLoadingVisibility(true)
                is CloudLoginState.Selection -> {
                    listAdapter.submitList(state.cloudProviderStates)
                    changeLoadingVisibility(false)
                }
            }
        }
    }

    private fun changeLoadingVisibility(isLoading: Boolean) {
        binding.cloudLoginRecyclerView.apply {
            makeItFiftyPercentMoreTransparentIf(isLoading)
            isEnabled = !isLoading
        }
        binding.cloudLoginLoadingProgress.isVisible = isLoading
    }
}
