package my.cardholder.ui.permission

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPermissionBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentPermissionBinding>(
    FragmentPermissionBinding::inflate
) {

    override val viewModel: PermissionViewModel by viewModels()

    override fun initViews() {
        binding.permissionGrantFab.setOnClickListener {
            viewModel.onGrantFabClicked()
        }
    }

    override fun collectData() {
    }
}
