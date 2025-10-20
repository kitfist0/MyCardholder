package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject
import kotlin.collections.indexOfFirst
import kotlin.collections.toMutableList

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            headerState = HeaderState(false),
            settingsItems = SettingId.entries.map { ListItem(it) },
        )
    )
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { isEnabled ->
                val cloudName = if (isEnabled) {
                    settingsRepository.cloudProvider.first().cloudName
                } else {
                    null
                }
                _state.update {
                    it.copy(
                        headerState = HeaderState(isEnabled, cloudName)
                    )
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.nightModeEnabled
            .onEach { isEnabled ->
                updateState(
                    predicate = { item -> item.id == SettingId.THEME },
                    update = {
                        ListItem(
                            id = SettingId.THEME,
                            options = listOf(
                                ListItem.Option("day", "Day", !isEnabled),
                                ListItem.Option("night", "Night", isEnabled),
                            )
                        )
                    }
                )
            }
            .launchIn(viewModelScope)

        settingsRepository.multiColumnListEnabled
            .onEach { isEnabled ->
                updateState(
                    predicate = { item -> item.id == SettingId.COLUMNS },
                    update = {
                        ListItem(
                            id = SettingId.COLUMNS,
                            options = listOf(
                                ListItem.Option("one", "1", !isEnabled),
                                ListItem.Option("two", "2", isEnabled),
                            )
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun onHeaderClicked() {
        viewModelScope.launch {
            if (settingsRepository.cloudSyncEnabled.first()) {
                navigate(SettingsFragmentDirections.fromSettingsToCloudLogout())
            } else {
                navigate(SettingsFragmentDirections.fromSettingsToCloudLogin())
            }
        }
    }

    private fun onItemClicked(settingId: SettingId) {
        when (settingId) {
            SettingId.THEME ->
                viewModelScope.launch {
                    val isEnabled = settingsRepository.nightModeEnabled.first()
                    settingsRepository.setNightModeEnabled(!isEnabled)
                }

            SettingId.COLUMNS ->
                viewModelScope.launch {
                    val isEnabled = settingsRepository.multiColumnListEnabled.first()
                    settingsRepository.setMultiColumnListEnabled(!isEnabled)
                }

            SettingId.CATEGORIES ->
                navigate(SettingsFragmentDirections.fromSettingsToCategoryList())

            SettingId.BACKUP ->
                navigate(SettingsFragmentDirections.fromSettingsToCardBackup())

            SettingId.COFFEE ->
                navigate(SettingsFragmentDirections.fromSettingsToCoffee())

            SettingId.ABOUT ->
                navigate(SettingsFragmentDirections.fromSettingsToInfo())
        }
    }

    private fun updateState(
        predicate: (ListItem) -> Boolean,
        update: (ListItem) -> ListItem,
    ) {
        _state.update {
            val prevList = it.settingsItems
            val index = prevList.indexOfFirst(predicate)
            val newList = if (index != -1) {
                prevList.toMutableList().apply { this[index] = update(this[index]) }
            } else {
                prevList
            }
            it.copy(settingsItems = newList)
        }
    }
}
