package my.cardholder.ui

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import my.cardholder.billing.PurchasedProductsProvider
import my.cardholder.data.CardRepository
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.CloudProvider
import my.cardholder.usecase.CloudDownloadUseCase
import my.cardholder.usecase.CloudUploadUseCase
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Result
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val cardRepository = mockk<CardRepository>(relaxed = true)
    private val cloudDownloadUseCase = mockk<CloudDownloadUseCase>(relaxed = true)
    private val cloudUploadUseCase = mockk<CloudUploadUseCase>(relaxed = true)
    private val purchasedProductsProvider = mockk<PurchasedProductsProvider>(relaxed = true)
    private val coffeeRepository = mockk<CoffeeRepository>(relaxed = true)
    private val networkChecker = mockk<NetworkChecker>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Default flows to avoid crashes during ViewModel initialization
        every { settingsRepository.appTheme } returns MutableStateFlow(AppTheme.SYSTEM)
        every { settingsRepository.cloudSyncEnabled } returns MutableStateFlow(false)
        every { settingsRepository.cloudProvider } returns MutableStateFlow(CloudProvider.GOOGLE)
        every { settingsRepository.latestSyncedBackupChecksum } returns MutableStateFlow(0L)
        every { cardRepository.checksumOfAllCards } returns MutableStateFlow(0L)
        every { purchasedProductsProvider.purchasedProducts } returns emptyFlow()
        every { cloudDownloadUseCase.execute(any(), any()) } returns emptyFlow()
        every { cloudUploadUseCase.execute(any(), any()) } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects purchased products and updates coffee repository`() = runTest {
        val purchasedIds = listOf("product_1", "product_2")
        every { purchasedProductsProvider.purchasedProducts } returns flowOf(purchasedIds)

        MainViewModel(
            cardRepository,
            cloudDownloadUseCase,
            cloudUploadUseCase,
            purchasedProductsProvider,
            coffeeRepository,
            networkChecker,
            settingsRepository
        )

        coVerify { coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds) }
    }

    @Test
    fun `cloud sync triggers download when enabled and network available`() = runTest {
        val syncEnabledFlow = MutableStateFlow(false)
        every { settingsRepository.cloudSyncEnabled } returns syncEnabledFlow
        every { networkChecker.isNetworkAvailable() } returns true
        every { cloudDownloadUseCase.execute(any(), any()) } returns flowOf(Result.Success(123L))

        MainViewModel(
            cardRepository,
            cloudDownloadUseCase,
            cloudUploadUseCase,
            purchasedProductsProvider,
            coffeeRepository,
            networkChecker,
            settingsRepository
        )

        syncEnabledFlow.value = true

        coVerify { cloudDownloadUseCase.execute(CloudProvider.GOOGLE, 0L) }
        coVerify { settingsRepository.setLatestSyncedBackupChecksum(123L) }
    }

    @Test
    fun `backup download log receives messages from download use case`() = runTest {
        val syncEnabledFlow = MutableStateFlow(true)
        every { settingsRepository.cloudSyncEnabled } returns syncEnabledFlow
        every { networkChecker.isNetworkAvailable() } returns true
        every { cloudDownloadUseCase.execute(any(), any()) } returns flowOf(
            Result.Loading("Starting..."),
            Result.Success(123L)
        )

        val viewModel = MainViewModel(
            cardRepository,
            cloudDownloadUseCase,
            cloudUploadUseCase,
            purchasedProductsProvider,
            coffeeRepository,
            networkChecker,
            settingsRepository
        )

        viewModel.backupDownloadLog.test {
            assertEquals("Starting...", awaitItem())
            assertEquals("Download completed", awaitItem())
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `cloud sync triggers upload when checksum changes and network available`() = runTest {
        val checksumFlow = MutableStateFlow(100L)
        every { settingsRepository.cloudSyncEnabled } returns flowOf(true)
        every { networkChecker.isNetworkAvailable() } returns true
        every { cardRepository.checksumOfAllCards } returns checksumFlow
        every { cloudUploadUseCase.execute(any(), any()) } returns flowOf(Result.Success(100L))

        MainViewModel(
            cardRepository,
            cloudDownloadUseCase,
            cloudUploadUseCase,
            purchasedProductsProvider,
            coffeeRepository,
            networkChecker,
            settingsRepository
        )

        checksumFlow.value = 200L

        coVerify { cloudUploadUseCase.execute(CloudProvider.GOOGLE, 200L) }
    }
}
