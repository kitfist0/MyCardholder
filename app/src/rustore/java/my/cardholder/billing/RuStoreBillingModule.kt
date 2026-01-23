// package my.cardholder.billing

// import android.content.Context
// import dagger.Binds
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.components.SingletonComponent
// import my.cardholder.util.NightModeChecker
// import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
// import ru.rustore.sdk.billingclient.presentation.BillingClientTheme
// import ru.rustore.sdk.billingclient.provider.BillingClientThemeProvider
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
// interface RuStoreBillingModule {

//     @Binds
//     fun bindPurchasedProductsProvider(assistant: RuStoreBillingAssistant): PurchasedProductsProvider

//     companion object {
//         private const val APP_ID = "2063489959"

//         @Provides
//         @Singleton
//         fun provideRuStoreBillingAssistant(
//             context: Context,
//             nightModeChecker: NightModeChecker,
//         ): RuStoreBillingAssistant {
//             val themeProvider = object : BillingClientThemeProvider {
//                 override fun provide(): BillingClientTheme {
//                     return if (nightModeChecker.isEnabled) {
//                         BillingClientTheme.Dark
//                     } else {
//                         BillingClientTheme.Light
//                     }
//                 }
//             }
//             val ruStoreBillingClient = RuStoreBillingClientFactory.create(
//                 context = context,
//                 consoleApplicationId = APP_ID,
//                 deeplinkScheme = "cardholder",
//                 themeProvider = themeProvider,
//             )
//             return RuStoreBillingAssistant(ruStoreBillingClient)
//         }
//     }
// }
