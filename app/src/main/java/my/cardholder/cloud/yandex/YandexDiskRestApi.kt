package my.cardholder.cloud.yandex

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@Keep
data class Link(
    val href: String,
    val method: String,
    val templated: Boolean,
)

@Keep
data class DiskFile(
    val path: String,
    val name: String,
    val type: String,
)

@Keep
data class DiskResources(
    @SerializedName("_embedded") val embedded: Embedded?,
    val items: List<DiskFile> = emptyList(),
)

@Keep
data class Embedded(
    val items: List<DiskFile>,
    val limit: Int,
    val offset: Int,
    val path: String,
    val total: Int,
)

interface YandexDiskRestApi {

    companion object {
        const val BASE_URL = "https://cloud-api.yandex.net/v1/disk/"
    }

    @GET("resources/upload")
    suspend fun getUploadLink(
        @Header("Authorization") token: String,
        @Query("path") path: String,
    ): Response<Link>

    @GET("resources/download")
    suspend fun getDownloadLink(
        @Header("Authorization") token: String,
        @Query("path") path: String
    ): Response<Link>

    @GET("resources")
    suspend fun listFiles(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
    ): Response<DiskResources>

    @DELETE("resources")
    suspend fun deleteFile(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("permanently") permanently: Boolean = true,
    )
}
