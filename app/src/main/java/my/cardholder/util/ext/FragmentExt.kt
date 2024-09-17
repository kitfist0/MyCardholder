package my.cardholder.util.ext

import android.graphics.Rect
import android.net.Uri
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import my.cardholder.util.Text

fun Fragment.getStatusBarHeight(): Int {
    val rect = Rect()
    requireActivity().window.decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}

fun Fragment.getActionBarSize(): Int {
    val typedArray = requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    val actionBarSize = typedArray.getDimensionPixelSize(0, -1)
    typedArray.recycle()
    return actionBarSize
}

fun Fragment.textToString(text: Text): String {
    return when (text) {
        is Text.Resource -> getString(text.resId)
        is Text.ResourceAndParams -> getString(text.resId, *text.params.toTypedArray())
        is Text.Simple -> text.text
    }
}

fun Fragment.getInputImageFromUri(uri: Uri?): InputImage? {
    return uri?.let { InputImage.fromFilePath(requireContext(), it) }
}
