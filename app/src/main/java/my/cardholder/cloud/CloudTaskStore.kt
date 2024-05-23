package my.cardholder.cloud

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@JvmInline
value class RemovalTask(val fileName: String)

@JvmInline
value class UploadTask(val fileNameAndContent: FileNameAndContent)

@Singleton
class CloudTaskStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        const val UPLOAD_TASK_KEY_PREFIX = "upload_task_"
        val REMOVAL_TASKS_KEY = stringSetPreferencesKey("removal_tasks")
    }

    val removalTasks: Flow<List<RemovalTask>> = dataStore.data.map { preferences ->
        preferences[REMOVAL_TASKS_KEY]?.toList()
            ?.map { RemovalTask(it) }
            .orEmpty()
    }

    val uploadTasks: Flow<List<UploadTask>> = dataStore.data.map { preferences ->
        preferences.asMap().keys
            .filter { it.name.startsWith(UPLOAD_TASK_KEY_PREFIX) }
            .map {
                val fileName = it.name.replace(UPLOAD_TASK_KEY_PREFIX, "")
                UploadTask(fileName to preferences[it].toString())
            }
    }

    suspend fun addRemovalTask(task: RemovalTask) = dataStore.edit { preferences ->
        removeUploadTaskBy(name = task.fileName)
        val set = preferences[REMOVAL_TASKS_KEY]?.toMutableSet()
            ?.also { it.add(task.fileName) }
            ?: setOf(task.fileName)
        preferences[REMOVAL_TASKS_KEY] = set
    }

    suspend fun addUploadTask(task: UploadTask) = dataStore.edit { preferences ->
        val fileNameAndContent = task.fileNameAndContent
        val key = stringPreferencesKey(UPLOAD_TASK_KEY_PREFIX + fileNameAndContent.getName())
        preferences[key] = fileNameAndContent.getContent()
    }

    suspend fun removeRemovalTaskBy(name: String) = dataStore.edit { preferences ->
        val set = preferences[REMOVAL_TASKS_KEY]?.toMutableSet()
            ?.apply { remove(name) }
            .orEmpty()
        preferences[REMOVAL_TASKS_KEY] = set
    }

    suspend fun removeUploadTaskBy(name: String) = dataStore.edit { preferences ->
        val key = stringPreferencesKey(UPLOAD_TASK_KEY_PREFIX + name)
        preferences.remove(key)
    }
}
