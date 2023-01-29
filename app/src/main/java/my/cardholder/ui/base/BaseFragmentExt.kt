package my.cardholder.ui.base

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import my.cardholder.util.ext.collectWhenStarted

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

fun Fragment.collectAndHandleBaseEvents(baseViewModel: BaseViewModel) {
    collectWhenStarted(baseViewModel.baseEvents) { event ->
        when (event) {
            is BaseEvent.Navigate -> event.extras
                ?.let { extras -> findNavController().navigate(event.direction, extras) }
                ?: findNavController().navigate(event.direction)
            is BaseEvent.NavigateUp ->
                findNavController().navigateUp()
            is BaseEvent.SnackMessage ->
                Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
            is BaseEvent.StartActivity -> event.uriString
                ?.let { uriString -> startActivity(Intent(event.action, Uri.parse(uriString))) }
                ?: startActivity(Intent(event.action))
        }
    }
}