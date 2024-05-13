package my.cardholder.cloud

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

typealias UploadTask = Pair<String, String>

@Singleton
class CloudFileUploadTaskStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        const val UPLOAD_TASK_KEY_PREFIX = "upload_task_"
    }

    val tasks: Flow<List<UploadTask>> = dataStore.data.map { preferences ->
        preferences.asMap().keys
            .filter { it.name.startsWith(UPLOAD_TASK_KEY_PREFIX) }
            .map { it.name to preferences[it].toString() }
    }

    suspend fun addTask(task: UploadTask) = dataStore.edit { preferences ->
        val key = stringPreferencesKey(task.first)
        preferences[key] = task.second
    }

    suspend fun removeTaskBy(name: String) = dataStore.edit { preferences ->
        val key = stringPreferencesKey(UPLOAD_TASK_KEY_PREFIX + name)
        preferences.remove(key)
    }
}
