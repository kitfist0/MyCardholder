package my.cardholder.ui.cardholder.viewer

import my.cardholder.data.model.Card

sealed class CardholderViewerState {
    object Loading : CardholderViewerState()
    data class Success(val card: Card) : CardholderViewerState()
}
