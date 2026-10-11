package my.cardholder.widget

import android.appwidget.AppWidgetManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.CardWidgetRepository
import javax.inject.Inject

@HiltViewModel
class CardWidgetConfigureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val cardWidgetRepository: CardWidgetRepository,
    private val cardWidgetManager: CardWidgetManager,
) : ViewModel() {

    val widgetId: Int =
        savedStateHandle[AppWidgetManager.EXTRA_APPWIDGET_ID] ?: AppWidgetManager.INVALID_APPWIDGET_ID

    // Null until the cards the widget already shows are loaded.
    private val selectedCardIds = MutableStateFlow<Set<Long>?>(null)

    val state: StateFlow<CardWidgetConfigureState> = combine(
        cardRepository.cardsAndCategories,
        selectedCardIds.filterNotNull(),
    ) { cardsAndCategories, selectedIds ->
        val items = cardsAndCategories.map { CardWidgetConfigureState.Item(it.card, it.card.id in selectedIds) }
        CardWidgetConfigureState(
            items = items,
            isSaveEnabled = items.any { it.isSelected },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CardWidgetConfigureState())

    private val savedChannel = Channel<Unit>()
    val savedEvent = savedChannel.receiveAsFlow()

    private var isSaving = false

    init {
        viewModelScope.launch {
            selectedCardIds.value = cardWidgetRepository.getWidgetCardIds(widgetId)
        }
    }

    fun onCardClicked(cardId: Long) {
        selectedCardIds.update { selectedIds ->
            selectedIds?.let { if (cardId in it) it - cardId else it + cardId }
        }
    }

    fun onSaveClicked() {
        if (isSaving) return
        val selectedIds = state.value.items.filter { it.isSelected }.map { it.card.id }.toSet()
        if (selectedIds.isEmpty()) return
        isSaving = true
        viewModelScope.launch {
            cardWidgetRepository.setWidgetCards(widgetId, selectedIds)
            cardWidgetManager.updateWidgets(intArrayOf(widgetId))
            savedChannel.send(Unit)
        }
    }
}
