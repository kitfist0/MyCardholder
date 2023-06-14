package my.cardholder.ui.coffee

import my.cardholder.data.model.Coffee

data class CoffeeState(
    val coffees: List<Coffee>,
    val launchPurchaseFlow: String?,
)
