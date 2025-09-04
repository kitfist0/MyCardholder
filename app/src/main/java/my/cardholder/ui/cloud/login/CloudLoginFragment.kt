package my.cardholder.ui.cloud.login

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCloudLoginBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
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

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CloudProviderAdapter {
            viewModel.onCloudProviderClicked(it)
        }
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
                is CloudLoginState.Loading -> {
                    enableOrDisableRecyclerView(false)
                    binding.cloudLoginLoadingProgress.isVisible = true
                }

                is CloudLoginState.Selection -> {
                    enableOrDisableRecyclerView(true)
                    binding.cloudLoginLoadingProgress.isVisible = false
                    listAdapter.submitList(state.cloudProviderStates)
                }
            }
        }
    }

    private fun enableOrDisableRecyclerView(areEnabled: Boolean) {
        val alphaValue = if (areEnabled) 1.0f else 0.5f
        binding.cloudLoginRecyclerView.apply {
            alpha = alphaValue
            isEnabled = areEnabled
        }
    }
}
