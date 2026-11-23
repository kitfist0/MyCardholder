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
import my.cardholder.data.BrandRepository
import my.cardholder.data.CardRepository
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.data.model.LogoSuggestion
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.ImageUrlValidator
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Text
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CardEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
    private val imageUrlValidator: ImageUrlValidator,
    private val networkChecker: NetworkChecker,
    private val brandRepository: BrandRepository,
) : BaseViewModel() {

    private companion object {
        const val LOGO_VALIDATION_DELAY = 2000L
    }

    private val cardId = CardEditFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    /**
     * Typed text is the state of its field. Saving it echoes back from the database later,
     * possibly after more letters are typed, so the typed text takes precedence over the saved one.
     */
    private var cardName: String? = null
    private var cardLogo: String? = null

    /** The name is saved as it's typed, so erasing it would leave a part of it, e.g. its first letter */
    private var cardNameWhenOpened: String? = null

    /**
     * Logos are suggested only once the name is edited: the field gets the saved name when the
     * screen is opened, and suggestions for it would just be in the way of other changes.
     */
    private var isCardNameEdited = false

    /**
     * Whether the name was erased since the logo was last set. Erasing it means the card is named
     * anew, so logos are suggested again even if the card has one, e.g. a wrongly chosen one.
     */
    private var isCardNameErasedSinceLogo = false
    private var cardCategoryName: String? = null
    private var cardColor: String? = null
    private var cardFormat: String? = null

    private var logoValidationJob: Job? = null

    /**
     * Typed values are saved one after another: concurrent saves could finish in any order,
     * so an older value could overwrite the last one. Values still waiting are replaced by newer ones.
     */
    private val cardNameToSave = MutableStateFlow<String?>(null)
    private val cardLogoToSave = MutableStateFlow<LogoToSave?>(null)

    /** A wrapper, so that erasing the logo (null) is a value to save too */
    private data class LogoToSave(val logo: String?)

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val categoryNames = categoryRepository.getCategoryNames()
                val card = cardAndCategory.card
                if (cardNameWhenOpened == null) {
                    cardNameWhenOpened = card.name
                }
                val shownCardName = cardName ?: card.name
                val shownCardLogo = cardLogo ?: card.logo.orEmpty()
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
                    cardName = shownCardName,
                    cardNameIsEmpty = shownCardName.isBlank(),
                    cardContent = card.content,
                    cardCategoryName = cardAndCategory.category?.name ?: Category.NULL_NAME,
                    cardCategoryNames = listOf(Category.NULL_NAME).plus(categoryNames),
                    cardComment = card.comment.orEmpty(),
                    cardColor = card.color,
                    cardLogo = shownCardLogo,
                    logoSuggestions = logoSuggestions(shownCardName, shownCardLogo),
                    barcodeFormatName = card.format.toString(),
                )
                if (card.barcodeFile?.exists() == false) {
                    showSnack(Text.Resource(R.string.card_edit_invalid_value_error_message))
                }
            }
            .launchIn(viewModelScope)

        cardNameToSave
            .filterNotNull()
            .onEach { name -> cardRepository.updateCardName(cardId, name) }
            .launchIn(viewModelScope)

        cardLogoToSave
            .filterNotNull()
            .onEach { logoToSave ->
                cardRepository.updateCardLogo(cardId, logoToSave.logo)
                validateLogo(logoToSave.logo)
            }
            .launchIn(viewModelScope)
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onCardNameChanged(changedName: String?) {
        if (changedName == null || cardName == changedName) {
            return
        }
        cardName = changedName
        if (changedName != cardNameWhenOpened) {
            isCardNameEdited = true
        }
        val nameIsEmpty = changedName.isBlank()
        if (nameIsEmpty) {
            isCardNameErasedSinceLogo = true
        }
        _state.update { state ->
            (state as? CardEditState.Success)?.copy(
                cardName = changedName,
                cardNameIsEmpty = nameIsEmpty,
                logoSuggestions = logoSuggestions(changedName, state.cardLogo),
            ) ?: state
        }
        // Until a new name is entered, the card has the name it had when the screen was opened
        val nameToSave = if (nameIsEmpty) cardNameWhenOpened else changedName
        nameToSave ?: return
        cardNameToSave.value = nameToSave
    }

    fun onCardContentClicked(extras: Navigator.Extras) {
        navigate(CardEditFragmentDirections.fromCardEditToCardContent(cardId), extras)
    }

    fun onCardContentScanIconClicked() {
        navigate(CardEditFragmentDirections.fromCardEditToCardScan(cardId))
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
        cardLogo = changedLogo
        isCardNameErasedSinceLogo = false
        _state.update { state ->
            (state as? CardEditState.Success)?.copy(
                cardLogo = changedLogo,
                logoSuggestions = logoSuggestions(state.cardName, changedLogo),
            ) ?: state
        }
        logoValidationJob?.cancel()
        logoValidationJob = null
        cardLogoToSave.value = LogoToSave(changedLogo.ifBlank { null })
    }

    /** Like an autocompletion, the suggestion completes the name too */
    fun onLogoSuggestionClicked(suggestion: LogoSuggestion) {
        onCardNameChanged(suggestion.name)
        onCardLogoChanged(suggestion.logo)
    }

    private fun logoSuggestions(name: String, logo: String): List<LogoSuggestion> {
        val canHaveLogo = logo.isBlank() || isCardNameErasedSinceLogo
        return if (isCardNameEdited && canHaveLogo) brandRepository.suggestLogos(name) else emptyList()
    }

    /** Checks the logo once it stops changing */
    private fun validateLogo(logo: String?) {
        logo ?: return
        logoValidationJob?.cancel()
        logoValidationJob = viewModelScope.launch {
            delay(LOGO_VALIDATION_DELAY.milliseconds)
            if (!imageUrlValidator.isValid(logo)) {
                if (networkChecker.isNetworkAvailable()) {
                    showToast(Text.Resource(R.string.card_edit_invalid_image_link_error_message))
                } else {
                    showToast(Text.Resource(R.string.card_edit_no_connection_error_message))
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
