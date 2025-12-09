package my.cardholder.data.model

import my.cardholder.R

enum class PaymentOption {
    ANNUAL,
    MONTHLY;

    companion object {
        fun PaymentOption.getTitleStringRes(): Int = when (this) {
            ANNUAL -> R.string.payment_option_annual_title_text
            MONTHLY -> R.string.payment_option_monthly_title_text
        }
    }
}
