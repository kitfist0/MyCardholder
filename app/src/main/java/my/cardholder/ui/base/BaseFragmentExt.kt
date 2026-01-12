package my.cardholder.ui.base

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString
import androidx.core.net.toUri

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

fun Fragment.collectAndHandleBaseEvents(baseViewModel: BaseViewModel) {
    collectWhenStarted(baseViewModel.baseEvents) { event ->
        when (event) {
            is BaseEvent.Navigate ->
                navigateSafely(event.direction, event.extras)

            is BaseEvent.NavigateUp ->
                findNavController().navigateUp()

            is BaseEvent.SnackMessage ->
                (this as? BaseFragment<*>)?.showSnack(event.text)
                    ?: throw RuntimeException("Snackbar should only be shown in fragments!")

            is BaseEvent.ShowOkSnack ->
                (this as? BaseFragment<*>)?.showOkSnack(event.text, event.actionCode)
                    ?: throw RuntimeException("Snackbar should only be shown in fragments!")

            is BaseEvent.StartActivity -> event.uriString
                ?.let { uriString -> startActivity(Intent(event.action, uriString.toUri())) }
                ?: startActivity(Intent(event.action))

            is BaseEvent.ToastMessage ->
                Toast.makeText(requireContext(), textToString(event.text), Toast.LENGTH_LONG).show()
        }
    }
}

private fun Fragment.navigateSafely(direction: NavDirections, extras: Navigator.Extras?) {
    findNavController().apply {
        currentDestination?.getAction(direction.actionId) ?: return
        extras?.let { navigate(direction, it) } ?: navigate(direction)
    }
}
