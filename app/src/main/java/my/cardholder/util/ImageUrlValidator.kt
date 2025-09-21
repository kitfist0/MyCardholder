package my.cardholder.util

import android.content.Context
import jakarta.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImageUrlValidator @Inject constructor(
    private val context: Context,
) {
    suspend fun isValid(url: String): Boolean = suspendCoroutine { continuation ->
        LogoLoader(context).load(
            imageUrl = url,
            sizePx = 1,
            cornerRadiusPx = 0,
            onSuccess = {
                continuation.resume(true)
            },
            onError = {
                continuation.resume(false)
            }
        )
    }
}
