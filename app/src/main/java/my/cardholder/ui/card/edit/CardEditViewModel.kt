package my.cardholder.ui.card.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.ImageUrlValidator
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CardEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
    private val imageUrlValidator: ImageUrlValidator,
    private val networkChecker: NetworkChecker,
) : BaseViewModel() {

    private companion object {
        const val LOGO_VALIDATION_DELAY_MILLIS = 2000L
    }

    private val cardId = CardEditFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private var cardName: String? = null
    private var cardCategoryName: String? = null
    private var cardColor: String? = null
    private var cardLogo: String? = null
    private var cardFormat: String? = null

    private var logoValidationJob: Job? = null

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val categoryNames = categoryRepository.getCategoryNames()
                val card = cardAndCategory.card
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
                    cardName = card.name,
                    cardContent = card.content,
                    cardCategoryName = cardAndCategory.category?.name ?: Category.NULL_NAME,
                    cardCategoryNames = listOf(Category.NULL_NAME).plus(categoryNames),
                    cardComment = card.comment.orEmpty(),
                    cardColor = card.color,
                    cardLogo = card.logo.orEmpty(),
                    barcodeFormatName = card.format.toString(),
                )
                if (card.barcodeFile?.exists() == false) {
                    showSnack(Text.Resource(R.string.card_edit_invalid_value_error_message))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteCardButtonClicked() {
        navigate(CardEditFragmentDirections.fromCardEditToDeleteCard(cardId))
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onCardNameChanged(changedName: String?) {
        if (changedName == null || cardName == changedName) {
            return
        }
        cardName = changedName
        viewModelScope.launch {
            cardRepository.updateCardName(cardId, changedName)
        }
    }

    fun onCardContentClicked(extras: Navigator.Extras) {
        navigate(CardEditFragmentDirections.fromCardEditToCardContent(cardId), extras)
    }

    fun onCardCategoryNameChanged(changedCategoryName: String?) {
        if (changedCategoryName == null || cardCategoryName == changedCategoryName) {
            return
        }
        cardCategoryName = changedCategoryName
        viewModelScope.launch {
            val categoryId = categoryRepository.getCategoryIdByName(changedCategoryName)
            cardRepository.updateCardCategoryId(cardId, categoryId)
        }
    }

    fun onCardColorChanged(changedColor: String?) {
        if (changedColor == null || cardColor == changedColor) {
            return
        }
        cardColor = changedColor
        viewModelScope.launch {
            cardRepository.updateCardColor(cardId, changedColor)
        }
    }

    fun onCardCommentClicked(extras: Navigator.Extras) {
        navigate(CardEditFragmentDirections.fromCardEditToCardComment(cardId), extras)
    }

    fun onCardLogoHelpIconClicked() {
        navigate(CardEditFragmentDirections.fromCardEditToCardLogo())
    }

    fun onCardLogoChanged(changedLogo: String?) {
        if (changedLogo == null || cardLogo == changedLogo) {
            return
        }
        cardLogo = changedLogo.ifBlank { null }

        logoValidationJob?.cancel()
        logoValidationJob = null
        logoValidationJob = viewModelScope.launch {
            cardRepository.updateCardLogo(cardId, cardLogo)
            cardLogo?.let {
                delay(LOGO_VALIDATION_DELAY_MILLIS)
                if (!imageUrlValidator.isValid(it)) {
                    if (networkChecker.isNetworkAvailable()) {
                        showToast(Text.Resource(R.string.card_edit_invalid_image_link_error_message))
                    } else {
                        showToast(Text.Resource(R.string.card_edit_no_connection_error_message))
                    }
                }
            }
        }
    }

    fun onCardFormatChanged(changedFormat: String?) {
        if (changedFormat == null || cardFormat == changedFormat) {
            return
        }
        cardFormat = changedFormat
        viewModelScope.launch {
            cardRepository.updateCardFormat(cardId, SupportedFormat.valueOf(changedFormat))
        }
    }
}
