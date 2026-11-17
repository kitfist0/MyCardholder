package my.cardholder.cloud.yandex

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class YandexCloudBackupAssistantTest {

    private val yandexDiskRestApi = mockk<YandexDiskRestApi>()
    private val yandexPreferences = mockk<YandexPreferences>()
    private val assistant = YandexCloudBackupAssistant(OkHttpClient(), yandexDiskRestApi, yandexPreferences)

    @Before
    fun setUp() {
        every { yandexPreferences.getToken() } returns "token"
    }

    @Test
    fun `backups are listed from app folder`() = runTest {
        coEvery { yandexDiskRestApi.listFiles(any(), any(), any(), any()) } returns
                Response.success(resources(diskFile("111"), diskFile("not a backup"), diskFile("222")))

        assertEquals(listOf(111L, 222L), assistant.getBackupChecksums().getOrThrow())
    }

    @Test
    fun `missing app folder means there are no backups`() = runTest {
        coEvery { yandexDiskRestApi.listFiles(any(), any(), any(), any()) } returns errorResponse(404)

        assertEquals(emptyList<Long>(), assistant.getBackupChecksums().getOrThrow())
    }

    @Test
    fun `failed listing is an error, not an empty folder`() = runTest {
        listOf(401, 429, 500, 503).forEach { code ->
            coEvery { yandexDiskRestApi.listFiles(any(), any(), any(), any()) } returns errorResponse(code)

            assertTrue("Error $code", assistant.getBackupChecksums().isFailure)
        }
    }

    @Test
    fun `failed listing while downloading is an error, not a backup replaced by another device`() = runTest {
        coEvery { yandexDiskRestApi.listFiles(any(), any(), any(), any()) } returns errorResponse(500)

        // Null would mean the backup is gone, and the sync would delete it without merging
        assertTrue(assistant.getBackupContent(111L).isFailure)
    }

    @Test
    fun `failed listing while deleting deletes nothing`() = runTest {
        coEvery { yandexDiskRestApi.listFiles(any(), any(), any(), any()) } returns errorResponse(500)

        assertTrue(assistant.deleteBackups(listOf(111L)).isFailure)
        coVerify(exactly = 0) { yandexDiskRestApi.deleteFile(any(), any(), any()) }
    }

    private fun resources(vararg files: DiskFile) = DiskResources(
        embedded = Embedded(items = files.toList(), limit = 1000, offset = 0, path = "app:/", total = files.size),
    )

    private fun diskFile(name: String) = DiskFile(path = "app:/$name", name = name, type = "file")

    private fun errorResponse(code: Int) = Response.error<DiskResources>(code, "".toResponseBody())
}
