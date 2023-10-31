package my.cardholder.ui.permission

import android.Manifest
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import androidx.activity.result.PickVisualMediaRequest
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

    private val barcodeFileSelectionRequest =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onBarcodeFileSelectionRequestResult(uri)
        }

    override val viewModel: PermissionViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            permissionScanBarcodeFileButton.setOnClickListener {
                viewModel.onScanBarcodeFileButtonClicked()
            }
            permissionGrantFab.setOnClickListener {
                viewModel.onGrantFabClicked()
            }
            (permissionImage.drawable as AnimatedVectorDrawable).let { anim ->
                anim.start()
                anim.registerAnimationCallback(
                    object : Animatable2.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            anim.start()
                        }
                    }
                )
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is PermissionState.Loading -> with(binding) {
                    permissionImage.isVisible = false
                    permissionRationaleText.text = null
                    permissionGrantFab.isVisible = false
                }
                is PermissionState.PermissionRequired -> with(binding) {
                    permissionImage.isVisible = true
                    permissionRationaleText.text = getString(state.rationaleTextStringRes)
                    permissionGrantFab.isVisible = true
                    if (state.launchBarcodeFileSelectionRequest) {
                        barcodeFileSelectionRequest.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        viewModel.onBarcodeFileSelectionRequestLaunched()
                    } else if (state.launchCameraPermissionRequest) {
                        cameraPermissionRequest.launch(CAMERA_PERMISSION)
                        viewModel.onCameraPermissionRequestLaunched()
                    }
                }
            }
        }
    }
}
