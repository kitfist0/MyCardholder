package my.cardholder.ui.card.crop

sealed class CardCropState {
    data class Selection(
        val selectedImageUri: String,
        val startProcessingEvent: Boolean,
    ) : CardCropState()

    data object Processing : CardCropState()
}
