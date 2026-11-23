package my.cardholder.data.model

/**
 * A brand whose cards people keep, with the [names] it is typed by. The [logo] is a website,
 * whose icon is the logo, or a link to an image.
 */
data class Brand(val logo: String, val names: List<String>) {
    val displayName: String get() = names.first()
}
