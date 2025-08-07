package my.cardholder.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import jakarta.inject.Inject

class ImageUrlValidator @Inject constructor(
    private val context: Context,
) {
    suspend fun isValid(url: String): Boolean =
        runCatching {
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(1, 1)
                .allowHardware(false)
                .build()
            context.imageLoader.execute(request).drawable
        }.getOrNull() != null
}
