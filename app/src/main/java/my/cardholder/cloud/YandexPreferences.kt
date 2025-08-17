package my.cardholder.cloud

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    fun put(login: String?, token: String?) {
        sharedPreferences.edit {
            putString(YANDEX_LOGIN_NAME_PREF_KEY, login)
            putString(YANDEX_OAUTH_TOKEN_PREF_KEY, token)
        }
    }

    fun clear() {
        sharedPreferences.edit {
            putString(YANDEX_LOGIN_NAME_PREF_KEY, null)
            putString(YANDEX_OAUTH_TOKEN_PREF_KEY, null)
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(YANDEX_OAUTH_TOKEN_PREF_KEY, null)
    }

    fun getLogin(): String? {
        return sharedPreferences.getString(YANDEX_LOGIN_NAME_PREF_KEY, null)
    }

    private companion object {
        const val YANDEX_LOGIN_NAME_PREF_KEY = "yandex_login_name"
        const val YANDEX_OAUTH_TOKEN_PREF_KEY = "yandex_oauth_token"
    }
}