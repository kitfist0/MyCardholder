package my.cardholder.ui.permission

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPermissionBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentPermissionBinding>(
    FragmentPermissionBinding::inflate
) {

    private companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShow = ActivityCompat
            .shouldShowRequestPermissionRationale(requireActivity(), CAMERA_PERMISSION)
        viewModel.onCameraPermissionRequestResult(
            isGranted = isGranted,
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
                    if (state.launchCameraPermissionRequest) {
                        cameraPermissionRequest.launch(CAMERA_PERMISSION)
                        viewModel.onCameraPermissionRequestLaunched()
                    }
                }
            }
        }
    }
}
