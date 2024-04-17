package my.cardholder.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

class GoogleCredentialWrapper(private val context: Context) {
    fun getCredential(scopes: Collection<String>): GoogleAccountCredential? {
        return GoogleSignIn.getLastSignedInAccount(context)?.let { signInAccount ->
            GoogleAccountCredential.usingOAuth2(context, scopes)
                .setSelectedAccount(signInAccount.account)
        }
    }
}
