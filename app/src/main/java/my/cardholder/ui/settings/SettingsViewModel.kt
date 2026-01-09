package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.NumOfColumns
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

        settingsRepository.appTheme
            .onEach { theme ->
                updateState(
                    predicate = { item -> item.id == SettingId.THEME },
                    update = {
                        SettingsItem(
                            id = SettingId.THEME,
                            iconRes = when (theme) {
                                AppTheme.DARK -> R.drawable.ic_dark_mode
                                AppTheme.LIGHT -> R.drawable.ic_light_mode
                                AppTheme.SYSTEM -> R.drawable.ic_routine
                            },
                            options = AppTheme.entries.map {
                                SettingsItem.Option(
                                    id = it.name,
                                    title = it.name.lowercase(),
                                    selected = theme.name == it.name,
                                )
                            }
                        )
                    }
                )
            }
            .launchIn(viewModelScope)

        settingsRepository.numOfColumns
            .onEach { numOfColumns ->
                updateState(
                    predicate = { item -> item.id == SettingId.COLUMNS },
                    update = {
                        SettingsItem(
                            id = SettingId.COLUMNS,
                            iconRes = when (numOfColumns) {
                                NumOfColumns.ONE -> R.drawable.ic_list_single_column
                                NumOfColumns.TWO -> R.drawable.ic_list_multi_column
                            },
                            options = NumOfColumns.entries.map {
                                SettingsItem.Option(
                                    id = it.name,
                                    title = it.intValue.toString(),
                                    selected = numOfColumns.name == it.name,
                                )
                            }
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
                    settingsRepository.setAppTheme(AppTheme.valueOf(optionId))
                }

            SettingId.COLUMNS ->
                viewModelScope.launch {
                    settingsRepository.setNumOfColumns(NumOfColumns.valueOf(optionId))
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
