package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject
import kotlin.collections.indexOfFirst
import kotlin.collections.toMutableList

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val COLUMNS_OPTION_ONE = "one"
        const val COLUMNS_OPTION_TWO = "two"
        const val THEME_OPTION_DAY = "day"
        const val THEME_OPTION_NIGHT = "night"
    }

    private val _state = MutableStateFlow(
        SettingsState(
            headerState = SettingsState.HeaderState(false),
            settingsItems = SettingId.entries.map {
                SettingsItem(
                    id = it,
                    iconRes = it.getImageRes(),
                )
            },
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
                        headerState = SettingsState.HeaderState(isEnabled, cloudName)
                    )
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.nightModeEnabled
            .onEach { isEnabled ->
                updateState(
                    predicate = { item -> item.id == SettingId.THEME },
                    update = {
                        SettingsItem(
                            id = SettingId.THEME,
                            iconRes = if (isEnabled) R.drawable.ic_dark_mode else R.drawable.ic_light_mode,
                            options = listOf(
                                SettingsItem.Option(THEME_OPTION_DAY, "Day", !isEnabled),
                                SettingsItem.Option(THEME_OPTION_NIGHT, "Night", isEnabled),
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
                        SettingsItem(
                            id = SettingId.COLUMNS,
                            iconRes = if (isEnabled) R.drawable.ic_list_multi_column else R.drawable.ic_list_single_column,
                            options = listOf(
                                SettingsItem.Option(COLUMNS_OPTION_ONE, "1", !isEnabled),
                                SettingsItem.Option(COLUMNS_OPTION_TWO, "2", isEnabled),
                            )
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    fun onHeaderClicked() {
        viewModelScope.launch {
            if (settingsRepository.cloudSyncEnabled.first()) {
                navigate(SettingsFragmentDirections.fromSettingsToCloudLogout())
            } else {
                navigate(SettingsFragmentDirections.fromSettingsToCloudLogin())
            }
        }
    }

    fun onItemWithoutOptionsClicked(settingId: SettingId) {
        when (settingId) {
            SettingId.CATEGORIES ->
                navigate(SettingsFragmentDirections.fromSettingsToCategoryList())

            SettingId.BACKUP ->
                navigate(SettingsFragmentDirections.fromSettingsToCardBackup())

            SettingId.COFFEE ->
                navigate(SettingsFragmentDirections.fromSettingsToCoffee())

            SettingId.ABOUT ->
                navigate(SettingsFragmentDirections.fromSettingsToInfo())

            else -> {
            }
        }
    }

    fun onItemOptionClicked(settingId: SettingId, optionId: String) {
        when (settingId) {
            SettingId.THEME ->
                viewModelScope.launch {
                    val nightModeEnabled = optionId == THEME_OPTION_NIGHT
                    settingsRepository.setNightModeEnabled(nightModeEnabled)
                }

            SettingId.COLUMNS ->
                viewModelScope.launch {
                    val multiColumnListEnabled = optionId == COLUMNS_OPTION_TWO
                    settingsRepository.setMultiColumnListEnabled(multiColumnListEnabled)
                }

            else -> {
            }
        }
    }

    private fun updateState(
        predicate: (SettingsItem) -> Boolean,
        update: (SettingsItem) -> SettingsItem,
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
