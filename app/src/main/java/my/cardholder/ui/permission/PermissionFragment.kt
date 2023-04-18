package my.cardholder.ui.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentScannerPermissionBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentScannerPermissionBinding>(
    FragmentScannerPermissionBinding::inflate
) {

    private val permissionRequestHandler = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val entry = resultMap.entries.first()
        val shouldShow =
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), entry.key)
        viewModel.onPermissionRequestResult(
            permission = entry.key,
            isGranted = entry.value,
            shouldShowRationale = shouldShow,
        )
    }

    override val viewModel: PermissionViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            permissionAddManuallyButton.setOnClickListener {
                viewModel.onAddManuallyButtonClicked()
            }
            permissionGrantFab.setOnClickListener {
                viewModel.onGrantFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is PermissionState.Loading -> with(binding) {
                    permissionAnimationView.isVisible = false
                    permissionRationaleText.text = null
                    permissionGrantFab.isVisible = false
                }
                is PermissionState.PermissionRequired -> with(binding) {
                    permissionAnimationView.isVisible = true
                    permissionRationaleText.text = getString(state.rationaleTextStringRes)
                    permissionGrantFab.isVisible = true
                    if (state.requestPermission) {
                        permissionRequestHandler.launch(arrayOf(state.requiredPermission))
                    }
                }
            }
        }
    }
}
