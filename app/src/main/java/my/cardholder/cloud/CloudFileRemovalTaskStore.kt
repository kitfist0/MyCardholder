package my.cardholder.cloud

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

typealias RemovalTask = String

@Singleton
class CloudFileRemovalTaskStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        val REMOVAL_TASKS_KEY = stringSetPreferencesKey("removal_tasks")
    }

    val tasks: Flow<List<RemovalTask>> = dataStore.data.map { preferences ->
        preferences[REMOVAL_TASKS_KEY]?.toList().orEmpty()
    }

    suspend fun addTask(task: RemovalTask) = dataStore.edit { preferences ->
        val set = preferences[REMOVAL_TASKS_KEY]?.toMutableSet()
            ?.also { it.add(task) }
            ?: setOf(task)
        preferences[REMOVAL_TASKS_KEY] = set
    }

    suspend fun removeTaskBy(name: String) = dataStore.edit { preferences ->
        val set = preferences[REMOVAL_TASKS_KEY]?.toMutableSet()
            ?.apply { remove(name) }
            .orEmpty()
        preferences[REMOVAL_TASKS_KEY] = set
    }
}
