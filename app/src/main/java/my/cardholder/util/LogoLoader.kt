package my.cardholder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import my.cardholder.util.logo.Favicons
import my.cardholder.util.logo.PaddedRoundedTransformation
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class LogoLoader(
    private val context: Context,
) {

    companion object {
        const val DEFAULT_BITMAP_LOAD_TIMEOUT_MS = 4000L
    }

    /**
     * Loads the logo as a bitmap of [sizePx], or returns null if it can't be loaded in time.
     * A logo loaded after the timeout is still cached, so it is there the next time.
     */
    suspend fun loadBitmap(
        imageUrl: String,
        sizePx: Int,
        paddingPx: Int,
        cornerRadiusPx: Int,
        timeoutMs: Long = DEFAULT_BITMAP_LOAD_TIMEOUT_MS,
    ): Bitmap? = withTimeoutOrNull(timeoutMs.milliseconds) {
        suspendCancellableCoroutine { continuation ->
            load(
                imageUrl = imageUrl,
                sizePx = sizePx,
                paddingPx = paddingPx,
                cornerRadiusPx = cornerRadiusPx,
                onSuccess = { drawable ->
                    if (continuation.isActive) {
                        continuation.resume(drawable.toBitmap(sizePx, sizePx))
                    }
                },
                onError = {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                },
            )
        }
    }

    /**
     * Loads the image of [imageUrl]. If it isn't an image, e.g. it's a website address, loads the
     * best icon of the website instead (see [Favicons]).
     */
    fun load(
        imageUrl: String?,
        sizePx: Int,
        paddingPx: Int,
        cornerRadiusPx: Int,
        onSuccess: (Drawable) -> Unit,
        onError: () -> Unit,
    ) {
        fun loadLogo(url: String?, onLogoError: () -> Unit) {
            val request = ImageRequest.Builder(context)
                .decoderFactory(SvgDecoder.Factory())
                .data(url)
                .size(sizePx, sizePx)
                .allowHardware(false)
                .transformations(
                    PaddedRoundedTransformation(
                        paddingPx = paddingPx,
                        cornerRadiusPx = cornerRadiusPx,
                    )
                )
                .target(
                    onSuccess = {
                        it.setBounds(0, 0, sizePx, sizePx)
                        onSuccess.invoke(it)
                    },
                    onError = { onLogoError() },
                )
                .build()
            context.imageLoader.enqueue(request)
        }

        val isDirectUrl = imageUrl?.startsWith("http://") == true || imageUrl?.startsWith("https://") == true
        val website = imageUrl?.let(Favicons::website)

        if (isDirectUrl || website == null) {
            loadLogo(imageUrl) {
                if (website != null) {
                    chooseFavicon(website) { faviconUrl ->
                        if (faviconUrl != null) loadLogo(faviconUrl, onError) else onError()
                    }
                } else {
                    onError()
                }
            }
        } else {
            chooseFavicon(website) { faviconUrl ->
                if (faviconUrl != null) loadLogo(faviconUrl, onError) else onError()
            }
        }
    }

    /** Finds the icon of the website of Google or Yandex, see [Favicons.choose] */
    private fun chooseFavicon(website: String, onChosen: (String?) -> Unit) {
        val googleUrl = Favicons.googleUrl(website)
        val yandexUrl = Favicons.yandexUrl(website)
        loadSize(googleUrl) { googleSizePx ->
            if (Favicons.isSharp(googleSizePx)) {
                onChosen(googleUrl)
                return@loadSize
            }
            loadSize(yandexUrl) { yandexSizePx ->
                val faviconUrl = when (Favicons.choose(googleSizePx, yandexSizePx)) {
                    Favicons.Source.GOOGLE -> googleUrl
                    Favicons.Source.YANDEX -> yandexUrl
                    null -> null
                }
                onChosen(faviconUrl)
            }
        }
    }

    /**
     * Loads the image in its own size, to know how sharp it is, or null if it can't be loaded.
     * The image is cached, so loading it again as the logo doesn't download it again.
     */
    private fun loadSize(url: String, onSize: (Int?) -> Unit) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .target(
                onSuccess = { drawable ->
                    onSize((drawable as? BitmapDrawable)?.bitmap?.width ?: drawable.intrinsicWidth)
                },
                onError = { onSize(null) },
            )
            .build()
        context.imageLoader.enqueue(request)
    }
}
