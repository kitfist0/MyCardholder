package my.cardholder.ui.settings

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.NumOfColumns
import my.cardholder.ui.base.BaseEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)

    // Используем UnconfinedTestDispatcher для немедленного выполнения корутин в тестах Flow
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Настраиваем поведение Flow в репозитории по умолчанию
        every { settingsRepository.cloudSyncEnabled } returns MutableStateFlow(false)
        every { settingsRepository.appTheme } returns MutableStateFlow(AppTheme.SYSTEM)
        every { settingsRepository.numOfColumns } returns MutableStateFlow(NumOfColumns.ONE)
        every { settingsRepository.cloudProvider } returns MutableStateFlow(CloudProvider.GOOGLE)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init updates state with default values from repository`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.headerState.cloudSyncEnabled)
            assertEquals(SettingId.entries.size, state.settingsItems.size)
        }
    }

    @Test
    fun `init updates state when cloud sync is enabled`() = runTest {
        every { settingsRepository.cloudSyncEnabled } returns MutableStateFlow(true)
        every { settingsRepository.cloudProvider } returns MutableStateFlow(CloudProvider.YANDEX)

        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertEquals(true, state.headerState.cloudSyncEnabled)
            assertEquals(CloudProvider.YANDEX.cloudName, state.headerState.cloudName)
        }
    }

    @Test
    fun `theme click updates app theme in repository`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.onItemOptionClicked(SettingId.THEME, AppTheme.DARK.name)

        coVerify { settingsRepository.setAppTheme(AppTheme.DARK) }
    }

    @Test
    fun `num of columns click updates num of columns in repository`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.onItemOptionClicked(SettingId.COLUMNS, NumOfColumns.TWO.name)

        coVerify { settingsRepository.setNumOfColumns(NumOfColumns.TWO) }
    }

    @Test
    fun `categories click invokes navigation event`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.baseEvents.test {
            viewModel.onItemWithoutOptionsClicked(SettingId.CATEGORIES)
            val event = awaitItem()
            assertTrue(event is BaseEvent.Navigate)
        }
    }

    @Test
    fun `cloud click invokes navigation event`() = runTest {
        every { settingsRepository.cloudSyncEnabled } returns MutableStateFlow(false)
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.baseEvents.test {
            viewModel.onHeaderClicked()
            val event = awaitItem()
            assertTrue(event is BaseEvent.Navigate)
        }
    }
}
