package my.cardholder.data.source

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private companion object {
        const val LATEST_VERSION = 8
        const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun everyOldVersionMigratesToLatestSchema() {
        (1 until LATEST_VERSION).forEach { version ->
            val name = "$TEST_DB-$version"
            helper.createDatabase(name, version).close()
            // Fails if a migration is missing or leads to a schema other than the exported one
            helper.runMigrationsAndValidate(name, LATEST_VERSION, true, *AppDatabase.MIGRATIONS).close()
        }
    }

    @Test
    fun cardsOfFirstVersionSurviveAllMigrations() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO categories (id, name) VALUES (1, 'Shops')")
            execSQL(
                "INSERT INTO cards (id, name, category_id, content, color, format, path) " +
                        "VALUES (1, 'First', 1, '111', '#EF5350', 'QR_CODE', NULL)"
            )
            execSQL(
                "INSERT INTO cards (id, name, category_id, content, color, format, path) " +
                        "VALUES (2, 'Second', NULL, '222', '#42A5F5', 'EAN_13', NULL)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, LATEST_VERSION, true, *AppDatabase.MIGRATIONS)

        db.query("SELECT name, category_id, content, position, is_pinned, sync_id FROM cards ORDER BY id").use { cursor ->
            assertEquals(2, cursor.count)
            val syncIds = mutableSetOf<String>()
            val positions = mutableSetOf<Int>()

            cursor.moveToFirst()
            assertEquals("First", cursor.getString(0))
            assertEquals(1L, cursor.getLong(1))
            assertEquals("111", cursor.getString(2))
            positions += cursor.getInt(3)
            assertEquals(0, cursor.getInt(4))
            syncIds += cursor.getString(5)

            cursor.moveToNext()
            assertEquals("Second", cursor.getString(0))
            assertTrue(cursor.isNull(1))
            assertEquals("222", cursor.getString(2))
            positions += cursor.getInt(3)
            syncIds += cursor.getString(5)

            assertEquals("Each card keeps its own position", setOf(0, 1), positions)
            assertEquals("Each card gets its own sync id", 2, syncIds.size)
            assertTrue("Sync ids are not empty", syncIds.none { it.isEmpty() })
        }
        db.query("SELECT name FROM categories").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Shops", cursor.getString(0))
        }
        db.close()
    }
}
