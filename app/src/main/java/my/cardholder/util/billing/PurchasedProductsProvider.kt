package my.cardholder.util.billing

import kotlinx.coroutines.flow.Flow

interface PurchasedProductsProvider {
    val purchasedProducts: Flow<List<ProductId>>
}
