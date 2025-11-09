package my.cardholder.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.NumOfColumns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        val APP_THEME_KEY = intPreferencesKey("app_theme")
        val CLOUD_SYNC_ENABLED_KEY = booleanPreferencesKey("cloud_sync_enabled")
        val CLOUD_PROVIDER_KEY = intPreferencesKey("cloud_provider")
        val EXPLANATION_BARCODE_ZOOM_KEY = booleanPreferencesKey("explanation_zoom")
        val EXPLANATION_CARD_SCAN_KEY = booleanPreferencesKey("explanation_scan")
        val LATEST_SYNCED_BACKUP_CHECKSUM_KEY = longPreferencesKey("latest_synced_checksum")
        val NUM_OF_COLUMNS_KEY = intPreferencesKey("columns_number")
    }

    suspend fun setAppTheme(theme: AppTheme) = dataStore.edit { preferences ->
        preferences[APP_THEME_KEY] = theme.ordinal
    }

    val appTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        val themeValue = preferences[APP_THEME_KEY] ?: AppTheme.SYSTEM.ordinal
        AppTheme.entries[themeValue]
    }

    suspend fun setCloudSyncEnabled(b: Boolean) = dataStore.edit { preferences ->
        preferences[CLOUD_SYNC_ENABLED_KEY] = b
    }

    val cloudSyncEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_ENABLED_KEY] ?: false
    }

    suspend fun setCloudProvider(cloud: CloudProvider) = dataStore.edit { preferences ->
        preferences[CLOUD_PROVIDER_KEY] = cloud.ordinal
    }

    val cloudProvider: Flow<CloudProvider> = dataStore.data.map { preferences ->
        preferences[CLOUD_PROVIDER_KEY]
            ?.let { CloudProvider.entries[it] }
            ?: CloudProvider.GOOGLE
    }

    suspend fun disableExplanationAboutBarcodeZoom() = dataStore.edit { preferences ->
        preferences[EXPLANATION_BARCODE_ZOOM_KEY] = false
    }

    val explanationAboutBarcodeZoomIsRequired: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EXPLANATION_BARCODE_ZOOM_KEY] ?: true
    }

    suspend fun disableExplanationAboutCardScan() = dataStore.edit { preferences ->
        preferences[EXPLANATION_CARD_SCAN_KEY] = false
    }

    val explanationAboutCardScanIsRequired: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EXPLANATION_CARD_SCAN_KEY] ?: true
    }

    suspend fun setLatestSyncedBackupChecksum(l: Long) = dataStore.edit { preferences ->
        preferences[LATEST_SYNCED_BACKUP_CHECKSUM_KEY] = l
    }

    val latestSyncedBackupChecksum: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LATEST_SYNCED_BACKUP_CHECKSUM_KEY]
    }

    suspend fun setNumOfColumns(numOfColumns: NumOfColumns) = dataStore.edit { preferences ->
        preferences[NUM_OF_COLUMNS_KEY] = numOfColumns.ordinal
    }

    val numOfColumns: Flow<NumOfColumns> = dataStore.data.map { preferences ->
        val numOfColumnsValue = preferences[NUM_OF_COLUMNS_KEY] ?: NumOfColumns.ONE.ordinal
        NumOfColumns.entries[numOfColumnsValue]
    }
}
