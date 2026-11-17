package my.cardholder.ui.settings

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import my.cardholder.R
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.NumOfColumns
import my.cardholder.theme.DynamicColorsManager
import my.cardholder.ui.base.BaseEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val dynamicColorsManager = mockk<DynamicColorsManager>()
    private val dynamicColorsEnabled = MutableStateFlow(false)

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
        every { settingsRepository.dynamicColorsEnabled } returns dynamicColorsEnabled
        every { dynamicColorsManager.isAvailable } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init updates state with default values from repository`() = runTest {
        val viewModel = createViewModel()

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

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertEquals(true, state.headerState.cloudSyncEnabled)
            assertEquals(CloudProvider.YANDEX.cloudName, state.headerState.cloudName)
        }
    }

    @Test
    fun `theme click updates app theme in repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.onItemOptionClicked(SettingId.THEME, AppTheme.DARK.name)

        coVerify { settingsRepository.setAppTheme(AppTheme.DARK) }
    }

    @Test
    fun `num of columns click updates num of columns in repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.onItemOptionClicked(SettingId.COLUMNS, NumOfColumns.TWO.name)

        coVerify { settingsRepository.setNumOfColumns(NumOfColumns.TWO) }
    }

    @Test
    fun `three columns setting is selected with its icon`() = runTest {
        every { settingsRepository.numOfColumns } returns MutableStateFlow(NumOfColumns.THREE)
        val viewModel = createViewModel()

        viewModel.state.test {
            val item = expectMostRecentItem().settingsItems.first { it.id == SettingId.COLUMNS }
            assertEquals(R.drawable.ic_list_three_column, item.iconRes)
            assertEquals(NumOfColumns.THREE.name, item.options.single { it.selected }.id)
        }
    }

    @Test
    fun `categories click invokes navigation event`() = runTest {
        val viewModel = createViewModel()

        viewModel.baseEvents.test {
            viewModel.onItemWithoutOptionsClicked(SettingId.CATEGORIES)
            val event = awaitItem()
            assertTrue(event is BaseEvent.Navigate)
        }
    }

    @Test
    fun `cloud click invokes navigation event`() = runTest {
        every { settingsRepository.cloudSyncEnabled } returns MutableStateFlow(false)
        val viewModel = createViewModel()

        viewModel.baseEvents.test {
            viewModel.onHeaderClicked()
            val event = awaitItem()
            assertTrue(event is BaseEvent.Navigate)
        }
    }

    @Test
    fun `dynamic colors item shows whether they are enabled`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf("on" to false, "off" to true), dynamicColorsOptions(viewModel))

        dynamicColorsEnabled.value = true
        assertEquals(listOf("on" to true, "off" to false), dynamicColorsOptions(viewModel))
    }

    @Test
    fun `selecting dynamic colors option saves it`() = runTest {
        val viewModel = createViewModel()

        viewModel.onItemOptionClicked(SettingId.DYNAMIC_COLORS, "on")
        coVerify { settingsRepository.setDynamicColorsEnabled(true) }

        viewModel.onItemOptionClicked(SettingId.DYNAMIC_COLORS, "off")
        coVerify { settingsRepository.setDynamicColorsEnabled(false) }
    }

    @Test
    fun `dynamic colors item is hidden where they are not supported`() = runTest {
        every { dynamicColorsManager.isAvailable } returns false

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.settingsItems.none { it.id == SettingId.DYNAMIC_COLORS })
    }

    private fun dynamicColorsItem(viewModel: SettingsViewModel) = viewModel.state.value.settingsItems
        .single { it.id == SettingId.DYNAMIC_COLORS }

    private fun dynamicColorsOptions(viewModel: SettingsViewModel) = dynamicColorsItem(viewModel)
        .options.map { it.id to it.selected }

    private fun createViewModel() = SettingsViewModel(settingsRepository, dynamicColorsManager)
}
