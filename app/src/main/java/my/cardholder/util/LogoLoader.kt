package my.cardholder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import my.cardholder.util.logo.PaddedRoundedTransformation
import java.net.URL
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

    fun load(
        imageUrl: String?,
        sizePx: Int,
        paddingPx: Int,
        cornerRadiusPx: Int,
        onSuccess: (Drawable) -> Unit,
        onError: () -> Unit,
    ) {
        val imageRequestBuilder = ImageRequest.Builder(context)
            .decoderFactory(SvgDecoder.Factory())
            .data(imageUrl)
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
                onError = {
                    val favIconUrl = fetchFavIconUrl(imageUrl)
                    val favIconRequestBuilder = ImageRequest.Builder(context)
                        .data(favIconUrl)
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
                            onError = {
                                onError.invoke()
                            }
                        )
                    context.imageLoader.enqueue(favIconRequestBuilder.build())
                }
            )
        context.imageLoader.enqueue(imageRequestBuilder.build())
    }

    private fun fetchFavIconUrl(url: String?): String? {
        url ?: return null
        val normalizedUrl = normalizeUrl(url)
        return getGoogleFaviconUrl(normalizedUrl)
    }

    private fun normalizeUrl(urlString: String): String {
        var url = urlString.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    private fun getGoogleFaviconUrl(urlString: String): String {
        val domain = runCatching { URL(urlString).host ?: urlString }
            .getOrDefault(urlString.replace(Regex("^(https?://)"), ""))
        return "https://www.google.com/s2/favicons?domain=$domain&sz=128"
    }
}
