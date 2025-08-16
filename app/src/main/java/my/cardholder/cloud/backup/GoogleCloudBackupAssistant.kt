package my.cardholder.cloud.backup

import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.cardholder.BuildConfig
import my.cardholder.cloud.backup.CloudBackupAssistant.Companion.fileNameToChecksum
import my.cardholder.util.GoogleCredentialWrapper
import java.io.ByteArrayOutputStream
import java.io.IOException

class GoogleCloudBackupAssistant(
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val gsonFactory: GsonFactory,
    private val netHttpTransport: NetHttpTransport,
) : CloudBackupAssistant {

    private companion object {
        const val APP_DATA_FOLDER = "appDataFolder"
        const val FIELDS = "id,name,createdTime"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    override suspend fun getBackupChecksum() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            val files = drive.getAppDataFolderFiles()
                .sortedBy { file -> file.name }
            if (files.isNotEmpty()) {
                val filesToDelete = files.subList(0, files.lastIndex)
                drive.deleteFiles(filesToDelete)
            }
            files.lastOrNull()?.getChecksum() ?: 0L
        }
    }

    override suspend fun getBackupContent(checksum: BackupChecksum) = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getAppDataFolderFiles()
                .find { file -> file.getChecksum() == checksum }
                ?.let {
                    val outputStream = ByteArrayOutputStream()
                    drive.files().get(it.id).executeMediaAndDownloadTo(outputStream)
                    String(outputStream.toByteArray())
                }
        }
    }

    override suspend fun deleteBackup() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            val files = drive.getAppDataFolderFiles()
            drive.deleteFiles(files)
        }
    }

    override suspend fun uploadBackup(content: String, checksum: BackupChecksum) = withContext(Dispatchers.IO) {
        deleteBackup()
        runCatching {
            val drive = getDriveOrThrow()
            val inputStreamContent = InputStreamContent(MIME_TYPE_TEXT, content.byteInputStream())
            val newFile = File()
            newFile.setName(checksum.toString())
            newFile.setParents(listOf(APP_DATA_FOLDER))
            drive.files().create(newFile, inputStreamContent).setFields(FIELDS).execute()
            Unit
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setFields("files($FIELDS)")
            .setSpaces(APP_DATA_FOLDER)
            .execute()?.files.orEmpty()
    }

    private fun Drive.deleteFiles(files: List<File>) {
        files.forEach { file -> files().delete(file.id).execute() }
    }

    private fun File.getChecksum(): BackupChecksum {
        return name.fileNameToChecksum()
    }

    private fun getDriveOrThrow(): Drive {
        return googleCredentialWrapper.getCredential()
            ?.let { credential ->
                Drive.Builder(netHttpTransport, gsonFactory, credential)
                    .setApplicationName(BuildConfig.APP_NAME)
                    .build()
            }
            ?: throw IOException("Google Drive is not initialized")
    }
}
