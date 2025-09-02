package my.cardholder.cloud.yandex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.cardholder.cloud.BackupChecksum
import my.cardholder.cloud.CloudBackupAssistant
import my.cardholder.cloud.CloudBackupAssistant.Companion.fileNameToChecksum
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class YandexCloudBackupAssistant(
    private val okHttpClient: OkHttpClient,
    private val yandexDiskRestApi: YandexDiskRestApi,
    private val yandexPreferences: YandexPreferences,
) : CloudBackupAssistant {

    private companion object {
        const val APP_FOLDER = "app:/MyCardholder"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    override suspend fun getBackupChecksum() = withContext(Dispatchers.IO) {
        runCatching {
            yandexDiskRestApi.listFiles(getOauthTokenOrThrow(), APP_FOLDER).body()
                ?.embedded?.items
                ?.maxByOrNull { it.getChecksum() }
                ?.getChecksum() ?: 0L
        }
    }

    override suspend fun getBackupContent(checksum: BackupChecksum) = withContext(Dispatchers.IO) {
        runCatching {
            val token = getOauthTokenOrThrow()
            val backupFilePath = yandexDiskRestApi.listFiles(token, APP_FOLDER).body()
                ?.embedded?.items
                ?.find { it.getChecksum() == checksum }
                ?.path
            backupFilePath?.let { path ->
                val href = yandexDiskRestApi.getDownloadLink(token, path).body()
                    ?.href
                    ?: throw IOException("Download error: href is null")
                val downloadRequestBuilder = Request.Builder().url(href)
                val downloadResponse = okHttpClient
                    .newCall(downloadRequestBuilder.build())
                    .execute()
                if (!downloadResponse.isSuccessful) {
                    throw IOException("Download error: error ${downloadResponse.code}")
                }
                downloadResponse.body.byteStream()
                    .bufferedReader()
                    .use { it.readText() }
            }
        }
    }

    override suspend fun deleteBackup() = withContext(Dispatchers.IO) {
        runCatching {
            val token = getOauthTokenOrThrow()
            val filesResponse = yandexDiskRestApi.listFiles(token, APP_FOLDER)
            val diskFiles = filesResponse.body()?.embedded?.items ?: emptyList()
            if (diskFiles.isNotEmpty()) deleteFiles(token, diskFiles)
        }
    }

    override suspend fun uploadBackup(
        content: String,
        checksum: BackupChecksum,
    ) = withContext(Dispatchers.IO) {
        val filePath = "$APP_FOLDER/$checksum"
        runCatching {
            val href = yandexDiskRestApi.getUploadLink(getOauthTokenOrThrow(), filePath).body()
                ?.href
                ?: throw IOException("Upload error: href is null")
            val uploadRequestBuilder = Request.Builder()
                .url(href)
                .put(content.toRequestBody(MIME_TYPE_TEXT.toMediaType()))
            val uploadResponse = okHttpClient
                .newCall(uploadRequestBuilder.build())
                .execute()
            if (!uploadResponse.isSuccessful) {
                throw IOException("Upload error: error ${uploadResponse.code}")
            }
        }
    }

    private suspend fun deleteFiles(token: String, diskFiles: List<DiskFile>) {
        diskFiles.forEach { diskFile ->
            yandexDiskRestApi.deleteFile(token, diskFile.path)
        }
    }

    private fun DiskFile.getChecksum(): BackupChecksum {
        return name.fileNameToChecksum()
    }

    private fun getOauthTokenOrThrow(): String {
        val token = yandexPreferences.getToken().orEmpty()
        if (token.isEmpty()) throw IOException("Empty token")
        return "OAuth $token"
    }
}
