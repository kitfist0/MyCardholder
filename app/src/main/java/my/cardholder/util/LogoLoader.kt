package my.cardholder.util

import android.content.Context
import android.graphics.drawable.Drawable
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import java.net.URL

class LogoLoader(
    private val context: Context,
) {

    fun load(
        imageUrl: String?,
        sizePx: Int,
        cornerRadiusPx: Int,
        onSuccess: (Drawable) -> Unit,
        onError: () -> Unit,
    ) {
        val imageRequestBuilder = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(sizePx, sizePx)
            .allowHardware(false)
            .transformations(RoundedCornersTransformation(cornerRadiusPx.toFloat()))
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
                        .transformations(RoundedCornersTransformation(cornerRadiusPx.toFloat()))
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
