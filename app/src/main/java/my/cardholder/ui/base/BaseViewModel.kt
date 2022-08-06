package my.cardholder.ui.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    open fun onBackPressed() {
    }
}
