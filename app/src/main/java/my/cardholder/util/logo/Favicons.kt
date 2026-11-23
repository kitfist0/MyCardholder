package my.cardholder.util.logo

import java.net.URL

/**
 * Icons of websites, for a logo given as a website address. Google knows most sites and has
 * sharp icons of them, but of many sites, e.g. many Russian ones, it has only a blurry 16px
 * icon or none, while Yandex has a sharp one. Written without Android, so that it runs in unit tests.
 */
object Favicons {

    enum class Source { GOOGLE, YANDEX }

    /** Smaller icons look blurry as logos */
    const val MIN_SHARP_SIZE_PX = 32

    // Yandex gives a 1px image instead of an error for a site it has no icon of
    private const val MIN_YANDEX_SIZE_PX = 2

    /** The website of the [address], e.g. "lukoil.ru" of "https://lukoil.ru/azs", or null */
    fun website(address: String): String? {
        val trimmed = address.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val url = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
        return runCatching { URL(url).host }.getOrNull()?.ifEmpty { null }
    }

    fun googleUrl(website: String) = "https://www.google.com/s2/favicons?domain=$website&sz=128"

    fun yandexUrl(website: String) = "https://favicon.yandex.net/favicon/v2/$website?size=120"

    /** Whether the icon of Google is good enough, so that the icon of Yandex isn't needed */
    fun isSharp(googleSizePx: Int?): Boolean = googleSizePx != null && googleSizePx >= MIN_SHARP_SIZE_PX

    /**
     * The source with the better icon, by the sizes of the icons, null of a source without an icon.
     * A blurry icon of Google is still better than none.
     */
    fun choose(googleSizePx: Int?, yandexSizePx: Int?): Source? = when {
        isSharp(googleSizePx) -> Source.GOOGLE
        yandexSizePx != null && yandexSizePx >= MIN_YANDEX_SIZE_PX -> Source.YANDEX
        googleSizePx != null -> Source.GOOGLE
        else -> null
    }
}
