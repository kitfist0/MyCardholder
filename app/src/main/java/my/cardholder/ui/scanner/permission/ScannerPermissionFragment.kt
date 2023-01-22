package my.cardholder.ui.scanner.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentScannerPermissionBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class ScannerPermissionFragment : BaseFragment<FragmentScannerPermissionBinding>(
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

    override val viewModel: ScannerPermissionViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun initViews() {
        binding.permissionGrantFab.setOnClickListener {
            viewModel.onGrantFabClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is ScannerPermissionState.Loading -> with(binding) {
                    permissionAnimationView.isVisible = false
                    permissionRationaleText.text = null
                    permissionGrantFab.isVisible = false
                }
                is ScannerPermissionState.PermissionRequired -> with(binding) {
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
