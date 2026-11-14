package my.cardholder.data.model

data class FormatSpec(
    val format: SupportedFormat,
    val name: String,
    val characters: FormatCharacters,
    val length: FormatLength = FormatLength.Unfixed,
) {
    companion object {
        fun FormatSpec.getFileName(): String {
            return name.lowercase()
                .replace(" ", "")
                .replace("-", "")
                .plus(".jpg")
        }
    }
}

enum class FormatCharacters {
    DIGITS,
    CODABAR,
    ALPHANUMERIC,
    ASCII_128,
    ASCII_255,
}

sealed interface FormatLength {
    data class Fixed(val value: Int) : FormatLength
    data object Even : FormatLength
    data object Unfixed : FormatLength
}
