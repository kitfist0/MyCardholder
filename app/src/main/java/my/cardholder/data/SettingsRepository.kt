package my.cardholder.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import my.cardholder.data.model.CloudProvider
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
        val CLOUD_PROVIDER_KEY = intPreferencesKey("cloud_provider")
        val EXPLANATION_BARCODE_ZOOM_KEY = booleanPreferencesKey("explanation_zoom_test0")
        val EXPLANATION_CARD_SCAN_KEY = booleanPreferencesKey("explanation_scan")
        val LATEST_SYNCED_BACKUP_CHECKSUM_KEY = longPreferencesKey("latest_synced_checksum")
        val MULTI_COLUMN_LIST_KEY = booleanPreferencesKey("multi_column_list")
        val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode")
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
