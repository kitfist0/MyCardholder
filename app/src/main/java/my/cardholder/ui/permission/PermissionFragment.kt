package my.cardholder.ui.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPermissionBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentPermissionBinding>(
    FragmentPermissionBinding::inflate
) {

    private val permissionsRequestHandler = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it.key)
            viewModel.onPermissionRequestResult(
                permission = it.key,
                isGranted = it.value,
                shouldShowRationale = shouldShow,
            )
        }
    }

    override val viewModel: PermissionViewModel by viewModels()

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
        viewModel.uiVisibilityState.collectWhenStarted { isVisible ->
            binding.permissionConstraintLayout.isVisible = isVisible
        }
        viewModel.requestPermissions.collectWhenStarted { permissions ->
            permissionsRequestHandler.launch(permissions)
        }
    }
}
