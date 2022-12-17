package my.cardholder.data.model

data class FormatSpec(
    val name: String,
    val characters: String,
    val length: String = "unfixed",
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
