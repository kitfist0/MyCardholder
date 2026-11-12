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
import my.cardholder.data.CoffeeRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.SyncRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.CloudProvider
import my.cardholder.usecase.CloudSyncUseCase
import my.cardholder.util.NetworkChecker
import my.cardholder.util.Result
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val cloudSyncUseCase = mockk<CloudSyncUseCase>(relaxed = true)
    private val purchasedProductsProvider = mockk<PurchasedProductsProvider>(relaxed = true)
    private val coffeeRepository = mockk<CoffeeRepository>(relaxed = true)
    private val networkChecker = mockk<NetworkChecker>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val syncRepository = mockk<SyncRepository>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val syncEnabledFlow = MutableStateFlow(false)
    private val localChecksumFlow = MutableStateFlow(100L)
    private val latestSyncedChecksumFlow = MutableStateFlow<Long?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { settingsRepository.appTheme } returns MutableStateFlow(AppTheme.SYSTEM)
        every { settingsRepository.cloudSyncEnabled } returns syncEnabledFlow
        every { settingsRepository.cloudProvider } returns MutableStateFlow(CloudProvider.GOOGLE)
        every { settingsRepository.latestSyncedBackupChecksum } returns latestSyncedChecksumFlow
        every { syncRepository.localChecksum } returns localChecksumFlow
        every { purchasedProductsProvider.purchasedProducts } returns emptyFlow()
        every { networkChecker.isNetworkAvailable() } returns true
        every { cloudSyncUseCase.execute(any()) } answers {
            // Like the real use case, a successful sync remembers the synced state
            latestSyncedChecksumFlow.value = localChecksumFlow.value
            flowOf(Result.Success(localChecksumFlow.value))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MainViewModel(
        cloudSyncUseCase,
        purchasedProductsProvider,
        coffeeRepository,
        networkChecker,
        settingsRepository,
        syncRepository,
    )

    @Test
    fun `init collects purchased products and updates coffee repository`() = runTest {
        val purchasedIds = listOf("product_1", "product_2")
        every { purchasedProductsProvider.purchasedProducts } returns flowOf(purchasedIds)

        createViewModel()
        advanceUntilIdle()

        coVerify { coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds) }
    }

    @Test
    fun `no sync when sync is disabled`() = runTest {
        createViewModel()
        advanceUntilIdle()

        verify(exactly = 0) { cloudSyncUseCase.execute(any()) }
    }

    @Test
    fun `sync starts immediately when enabled even without local changes`() = runTest {
        latestSyncedChecksumFlow.value = localChecksumFlow.value
        createViewModel()

        syncEnabledFlow.value = true
        runCurrent()

        verify(exactly = 1) { cloudSyncUseCase.execute(CloudProvider.GOOGLE) }
    }

    @Test
    fun `no sync without network`() = runTest {
        every { networkChecker.isNetworkAvailable() } returns false
        syncEnabledFlow.value = true

        createViewModel()
        advanceUntilIdle()

        verify(exactly = 0) { cloudSyncUseCase.execute(any()) }
    }

    @Test
    fun `local changes are synced once after debounce`() = runTest {
        syncEnabledFlow.value = true
        createViewModel()
        runCurrent()
        verify(exactly = 1) { cloudSyncUseCase.execute(any()) }

        localChecksumFlow.value = 200L
        advanceTimeBy(MainViewModel.LOCAL_CHANGES_DEBOUNCE_MS / 2)
        localChecksumFlow.value = 300L
        advanceTimeBy(MainViewModel.LOCAL_CHANGES_DEBOUNCE_MS / 2)
        runCurrent()
        verify(exactly = 1) { cloudSyncUseCase.execute(any()) }

        advanceUntilIdle()
        verify(exactly = 2) { cloudSyncUseCase.execute(any()) }
        assertEquals(300L, latestSyncedChecksumFlow.value)
    }

    @Test
    fun `change caused by sync itself does not trigger another sync`() = runTest {
        every { cloudSyncUseCase.execute(any()) } answers {
            // Merging a cloud backup changes the local state, which is then synced
            localChecksumFlow.value = 500L
            latestSyncedChecksumFlow.value = 500L
            flowOf(Result.Success(500L))
        }
        syncEnabledFlow.value = true

        createViewModel()
        advanceUntilIdle()

        verify(exactly = 1) { cloudSyncUseCase.execute(any()) }
    }

    @Test
    fun `initial sync progress is shown in the log`() = runTest {
        every { cloudSyncUseCase.execute(any()) } returns flowOf(
            Result.Loading("Starting..."),
            Result.Success(123L),
        )
        syncEnabledFlow.value = true

        val viewModel = createViewModel()

        viewModel.backupDownloadLog.test {
            assertEquals("Starting...", awaitItem())
            assertEquals("Sync completed", awaitItem())
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `initial sync error is shown in the log`() = runTest {
        every { cloudSyncUseCase.execute(any()) } returns flowOf(Result.Error(Exception("Boom")))
        syncEnabledFlow.value = true

        val viewModel = createViewModel()

        viewModel.backupDownloadLog.test {
            assertEquals("Sync error: Boom", awaitItem())
            assertEquals(null, awaitItem())
        }
    }
}
