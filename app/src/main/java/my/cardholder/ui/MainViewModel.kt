package my.cardholder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.cloud.CloudTaskStore
import my.cardholder.cloud.UploadTask
import my.cardholder.data.CardRepository
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.CardAndCategory.Companion.toFileNameAndContent
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    cardRepository: CardRepository,
    cloudTaskStore: CloudTaskStore,
    purchasedProductsProvider: PurchasedProductsProvider,
    settingsRepository: SettingsRepository,
    private val coffeeRepository: CoffeeRepository,
) : ViewModel() {

    val nightModeEnabled = settingsRepository.nightModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        viewModelScope.launch {
            purchasedProductsProvider.purchasedProducts.collect { purchasedIds ->
                coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
            }
        }
        cardRepository.cardsAndCategoriesForSync
            .onEach { cardsAndCategories ->
                cardsAndCategories.forEach { cardAndCategory ->
                    val fileNameAndContent = cardAndCategory.toFileNameAndContent()
                    cloudTaskStore.addUploadTask(UploadTask(fileNameAndContent))
                }
            }
            .launchIn(viewModelScope)
    }
}
