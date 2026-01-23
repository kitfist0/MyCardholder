// package my.cardholder.cloud.yandex

// import android.content.Context
// import android.content.Intent
// import androidx.activity.result.ActivityResult
// import com.yandex.authsdk.YandexAuthException
// import com.yandex.authsdk.YandexAuthLoginOptions
// import com.yandex.authsdk.YandexAuthOptions
// import com.yandex.authsdk.YandexAuthResult
// import com.yandex.authsdk.YandexAuthSdk
// import com.yandex.authsdk.YandexAuthSdkContract
// import com.yandex.authsdk.YandexAuthToken
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
// import my.cardholder.cloud.CloudSignInAssistant
// import my.cardholder.util.JwtTokenDecoder
// import kotlin.coroutines.resume
// import kotlin.coroutines.resumeWithException
// import kotlin.coroutines.suspendCoroutine

// class YandexCloudSignInAssistant(
//     private val context: Context,
//     private val jwtTokenDecoder: JwtTokenDecoder,
//     private val yandexAuthSdk: YandexAuthSdk,
//     private val yandexPreferences: YandexPreferences,
// ) : CloudSignInAssistant {

//     private companion object {
//         const val LOGIN_KEY_NAME = "login"
//     }

//     override val alreadySignedIn: Boolean
//         get() = !yandexPreferences.getLogin().isNullOrEmpty()

//     override val signedInAccountName: String?
//         get() = yandexPreferences.getLogin()

//     override val signInIntent: Intent
//         get() = YandexAuthSdk.Companion.create(YandexAuthOptions(context)).contract
//             .createIntent(context, YandexAuthLoginOptions())

//     override suspend fun onSignInResult(activityResult: ActivityResult): Result<Unit> {
//         val contract = YandexAuthSdkContract(YandexAuthOptions(context))
//         val yandexAuthResult = contract.parseResult(-1, activityResult.data)

//         return when (yandexAuthResult) {
//             is YandexAuthResult.Failure, YandexAuthResult.Cancelled ->
//                 Result.failure(Throwable(yandexAuthResult.javaClass.simpleName))

//             is YandexAuthResult.Success ->
//                 runCatching { decodeToken(yandexAuthResult.token) }
//         }
//     }

//     override suspend fun signOut(): Result<Unit> {
//         yandexPreferences.clear()
//         return Result.success(Unit)
//     }

//     private suspend fun decodeToken(token: YandexAuthToken) = withContext(Dispatchers.IO) {
//         suspendCoroutine { continuation ->
//             runCatching {
//                 val jwtToken = yandexAuthSdk.getJwt(token)
//                 val login = jwtTokenDecoder.decode(jwtToken)
//                     .find { it.has(LOGIN_KEY_NAME) }
//                     ?.get(LOGIN_KEY_NAME)?.toString()
//                 if (login.isNullOrEmpty()) throw YandexAuthException("No login")
//                 login to token.value
//             }.onSuccess { loginAndToken ->
//                 yandexPreferences.put(
//                     login = loginAndToken.first,
//                     token = loginAndToken.second,
//                 )
//                 continuation.resume(Unit)
//             }.onFailure {
//                 continuation.resumeWithException(it)
//             }
//         }
//     }
// }
