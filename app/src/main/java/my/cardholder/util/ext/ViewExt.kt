package my.cardholder.util.ext

import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import coil.load
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.google.android.material.textfield.TextInputLayout
import my.cardholder.R
import java.io.File

fun View.setupUniqueTransitionName(uniqueSuffix: Long) {
    ViewCompat.setTransitionName(this, transitionName.format(uniqueSuffix))
}

fun View.updateVerticalPaddingAfterApplyingWindowInsets(
    top: Boolean = true,
    bottom: Boolean = true,
) {
    setOnApplyWindowInsetsListener { view, windowInsets ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val systemWindowInsets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            view.updatePadding(
                top = if (top) systemWindowInsets.top else 0,
                bottom = if (bottom) systemWindowInsets.bottom else 0,
            )
        } else {
            @Suppress("DEPRECATION")
            view.updatePadding(
                top = if (top) windowInsets.systemWindowInsetTop else 0,
                bottom = if (bottom) windowInsets.systemWindowInsetBottom else 0,
            )
        }
        windowInsets
    }
}

fun ImageView.setBitmapFromAssets(fileName: String) {
    runCatching {
        val inputStream = context.assets.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        setImageBitmap(bitmap)
    }
}

fun ImageView.loadBarcodeImage(
    barcodeFileName: String,
    originalSize: Boolean = true,
) {
    val barcodeFile = File(context.filesDir, barcodeFileName)
    load(barcodeFile) {
        if (originalSize) {
            size(Size.ORIGINAL)
        }
        transformations(
            RoundedCornersTransformation(
                resources.getDimensionPixelSize(R.dimen.barcode_image_corners).toFloat()
            )
        )
    }
}

fun EditText.setTextAndSelectionIfRequired(text: String) {
    if (this.text.toString() != text) {
        setText(text)
        setSelection(text.length)
    }
}

fun TextInputLayout.setAutocompleteTextIfRequired(items: List<String>, item: String) {
    (editText as? AutoCompleteTextView)?.apply {
        if (text.toString() != item) {
            setText(item)
        }
        if (adapter == null) {
            setAdapter(ArrayAdapter(context, android.R.layout.select_dialog_item, items))
        }
    }
}
