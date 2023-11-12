package my.cardholder.util.billing

import ru.rustore.sdk.billingclient.model.purchase.PaymentResult

fun PaymentResult.toSimpleResult(productId: String): Result<String> {
    return when (this) {
        is PaymentResult.Cancelled -> Result.failure(Throwable("Cancelled"))
        is PaymentResult.Failure -> Result.failure(Throwable("Error $errorCode"))
        is PaymentResult.InvalidPaymentState -> Result.failure(Throwable("Invalid state"))
        is PaymentResult.Success -> Result.success(productId)
    }
}
