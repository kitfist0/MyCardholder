package my.cardholder.util

import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import coil.load
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import my.cardholder.R
import java.io.File

fun View.setupUniqueTransitionName(uniqueSuffix: Long) {
    ViewCompat.setTransitionName(this, transitionName.format(uniqueSuffix))
}

fun ImageView.loadBarcodeImage(
    barcodeFile: File,
    originalSize: Boolean = true,
) {
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
