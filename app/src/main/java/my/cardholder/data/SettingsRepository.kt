package my.cardholder.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import my.cardholder.util.NightModeChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val nightModeChecker: NightModeChecker,
) {

    private companion object {
        val CLOUD_SYNC_ENABLED_KEY = booleanPreferencesKey("cloud_sync_enabled")
        val MULTI_COLUMN_LIST_KEY = booleanPreferencesKey("multi_column_list")
        val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode")
    }

    suspend fun setCloudSyncEnabled(b: Boolean) = dataStore.edit { preferences ->
        preferences[CLOUD_SYNC_ENABLED_KEY] = b
    }

    val cloudSyncEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_ENABLED_KEY] ?: false
    }

    suspend fun setMultiColumnListEnabled(b: Boolean) = dataStore.edit { preferences ->
        preferences[MULTI_COLUMN_LIST_KEY] = b
    }

    val multiColumnListEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[MULTI_COLUMN_LIST_KEY] ?: false
    }

    suspend fun setNightModeEnabled(b: Boolean) = dataStore.edit { preferences ->
        preferences[NIGHT_MODE_KEY] = b
    }

    val nightModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NIGHT_MODE_KEY] ?: nightModeChecker.isEnabled
    }
}
