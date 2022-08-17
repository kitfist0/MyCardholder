package my.cardholder.ui.permission

import dagger.hilt.android.lifecycle.HiltViewModel
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() : BaseViewModel() {
    fun onGrantFabClicked() {
        navigate(PermissionFragmentDirections.fromPermissionToScanner())
    }
}
