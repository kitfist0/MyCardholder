package my.cardholder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("prefs")

class SettingsDataStore(context: Context) {

    private companion object {
        val MULTI_COLUMN_LIST_KEY = booleanPreferencesKey("multi_column_list")
        val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode")
    }

    private val dataStore = context.dataStore

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
        preferences[NIGHT_MODE_KEY] ?: false
    }
}
