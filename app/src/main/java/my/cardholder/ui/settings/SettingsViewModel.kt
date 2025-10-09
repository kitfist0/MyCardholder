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
            settingsItems = listOf(SettingsListItem.Header(false))
                .plus(
                    SettingsItem.entries.map { SettingsListItem.Item(it) }
                )
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
                updateState(
                    predicate = { item -> item is SettingsListItem.Header },
                    update = {
                        SettingsListItem.Header(
                            cloudSyncEnabled = isEnabled,
                            cloudName = cloudName,
                        )
                    },
                )
            }
            .launchIn(viewModelScope)

        settingsRepository.nightModeEnabled
            .onEach { isEnabled ->
                updateState(
                    predicate = { item -> item is SettingsListItem.Item && item.id == SettingsItem.THEME },
                    update = {
                        SettingsListItem.Item(
                            SettingsItem.THEME,
                            if (isEnabled) "Night" else "Day"
                        )
                    },
                )
            }
            .launchIn(viewModelScope)

        settingsRepository.multiColumnListEnabled
            .onEach { isEnabled ->
                updateState(
                    predicate = { item -> item is SettingsListItem.Item && item.id == SettingsItem.COLUMNS },
                    update = {
                        SettingsListItem.Item(
                            SettingsItem.COLUMNS,
                            if (isEnabled) "2" else "1"
                        )
                    },
                )
            }
            .launchIn(viewModelScope)
    }

    fun onListItemClicked(settingsListItem: SettingsListItem) {
        when (settingsListItem) {
            is SettingsListItem.Header -> onHeaderClicked()
            is SettingsListItem.Item -> onItemClicked(settingsListItem.id)
        }
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

    private fun onItemClicked(settingsItem: SettingsItem) {
        when (settingsItem) {
            SettingsItem.THEME ->
                viewModelScope.launch {
                    val isEnabled = settingsRepository.nightModeEnabled.first()
                    settingsRepository.setNightModeEnabled(!isEnabled)
                }

            SettingsItem.COLUMNS ->
                viewModelScope.launch {
                    val isEnabled = settingsRepository.multiColumnListEnabled.first()
                    settingsRepository.setMultiColumnListEnabled(!isEnabled)
                }

            SettingsItem.CATEGORIES ->
                navigate(SettingsFragmentDirections.fromSettingsToCategoryList())

            SettingsItem.BACKUP ->
                navigate(SettingsFragmentDirections.fromSettingsToCardBackup())

            SettingsItem.COFFEE ->
                navigate(SettingsFragmentDirections.fromSettingsToCoffee())

            SettingsItem.ABOUT ->
                navigate(SettingsFragmentDirections.fromSettingsToInfo())
        }
    }

    private fun updateState(
        predicate: (SettingsListItem) -> Boolean,
        update: (SettingsListItem) -> SettingsListItem,
    ) {
        _state.update {
            val prevList = it.settingsItems
            val index = prevList.indexOfFirst(predicate)
            val newList = if (index != -1) {
                prevList.toMutableList().apply { this[index] = update(this[index]) }
            } else {
                prevList
            }
            it.copy(
                settingsItems = newList,
            )
        }
    }
}
