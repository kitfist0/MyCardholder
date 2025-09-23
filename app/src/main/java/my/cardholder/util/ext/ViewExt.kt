package my.cardholder.util.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.google.android.material.textfield.TextInputLayout
import my.cardholder.R
import java.io.File
import androidx.core.graphics.toColorInt
import com.google.android.material.color.MaterialColors
import my.cardholder.util.LogoLoader

fun EditText.setStartColoredSquareIcon(color: String) {
    val sizePx = dpToPx(24)
    val cornerRadiusPx = dpToPx(4).toFloat()

    val radii = floatArrayOf(
        cornerRadiusPx, cornerRadiusPx,
        cornerRadiusPx, cornerRadiusPx,
        cornerRadiusPx, cornerRadiusPx,
        cornerRadiusPx, cornerRadiusPx
    )
    val squareDrawable = object : ShapeDrawable(RoundRectShape(radii, null, null)) {
        init {
            paint.color = color.toColorInt()
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
            intrinsicWidth = sizePx
            intrinsicHeight = sizePx
        }
    }
    squareDrawable.setBounds(0, 0, sizePx, sizePx)
    setCompoundDrawablesRelative(squareDrawable, null, null, null)
}

fun EditText.setStartIconFromUrl(
    imageUrl: String?,
    @DrawableRes errorDrawableRes: Int,
) {
    val sizePx = dpToPx(24)
    val cornerRadiusPx = dpToPx(4)

    LogoLoader(context).load(
        imageUrl = imageUrl,
        sizePx = sizePx,
        cornerRadiusPx = cornerRadiusPx,
        onSuccess = {
            it.setBounds(0, 0, sizePx, sizePx)
            setCompoundDrawablesRelative(it, null, null, null)
        },
        onError = {
            val backColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainer, Color.LTGRAY)
            val backDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(backColor)
                cornerRadius = cornerRadiusPx.toFloat()
            }
            val imageDrawable = AppCompatResources.getDrawable(context, errorDrawableRes)?.mutate()
            val insetSizePx = dpToPx(1)
            val errorDrawable = LayerDrawable(arrayOf(backDrawable, imageDrawable))
            errorDrawable.setLayerInset(1, insetSizePx, insetSizePx, insetSizePx, insetSizePx)
            errorDrawable.setBounds(0, 0, sizePx, sizePx)
            setCompoundDrawablesRelative(errorDrawable, null, null, null)
        }
    )
}

fun TextView.setStartEndCompoundDrawables(
    @DrawableRes startDrawableResId: Int? = null,
    @DrawableRes endDrawableResId: Int? = null,
) {
    val startDrawable = startDrawableResId?.let { ContextCompat.getDrawable(context, it) }
    val endDrawable = endDrawableResId?.let { ContextCompat.getDrawable(context, it) }
    setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, endDrawable, null)
}

@Suppress("MagicNumber")
fun View.animateVisibilityChange() {
    val finalVisibility = !isVisible
    isVisible = true
    alpha = if (finalVisibility) 0f else 1f
    animate()
        .setDuration(300)
        .alpha(if (finalVisibility) 1f else 0f)
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isVisible = finalVisibility
                }
            }
        )
        .start()
}

fun View.dpToPx(sizeDp: Int): Int = (sizeDp * resources.displayMetrics.density).toInt()

fun View.setupUniqueTransitionName(uniqueSuffix: Long) {
    ViewCompat.setTransitionName(this, transitionName.format(uniqueSuffix))
}

fun View.updateVerticalPaddingAfterApplyingWindowInsets(
    top: Boolean = true,
    bottom: Boolean = true,
) {
    setOnApplyWindowInsetsListener { view, windowInsets ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(
                top = if (top) bars.top else 0,
                bottom = if (bottom) bars.bottom else 0,
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

fun List<View>.toNavExtras(): FragmentNavigator.Extras {
    val sharedElements = this.associateWith { view -> view.transitionName }
    return FragmentNavigator.Extras.Builder()
        .addSharedElements(sharedElements)
        .build()
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
    barcodeFile: File?,
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

fun ImageView.loadLogoImage(
    logoUrl: String?,
    @DrawableRes defaultDrawableRes: Int,
) {
    logoUrl?.ifEmpty { null }
        ?.let {
            val sizePx = dpToPx(128)
            val cornerRadiusPx = dpToPx(16)
            LogoLoader(context).load(
                imageUrl = logoUrl,
                sizePx = sizePx,
                cornerRadiusPx = cornerRadiusPx,
                onSuccess = { setImageDrawable(it) },
                onError = { setImageResource(defaultDrawableRes) }
            )
        }
        ?: setImageResource(defaultDrawableRes)
}

fun EditText.onImeActionDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun EditText.setTextAndSelectionIfRequired(text: String) {
    if (this.text.toString() != text) {
        setText(text)
        setSelection(text.length)
    }
}

fun <T> AutoCompleteTextView.setDefaultAdapter(values: List<T>) {
    setAdapter(ArrayAdapter(context, android.R.layout.select_dialog_item, values))
}

fun RecyclerView.updateSpanCountIfRequired(count: Int) {
    (layoutManager as? GridLayoutManager)?.apply {
        if (spanCount != count) spanCount = count
    }
}

fun TextInputLayout.showSoftInput() {
    requestFocus()
    (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}
