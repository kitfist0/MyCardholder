package my.cardholder.util

import android.util.Base64
import org.json.JSONObject

class JwtTokenDecoder {
    fun decode(jwt: String): List<JSONObject> {
        val jsonObjects = mutableListOf<JSONObject>()
        val parts = jwt.split("[.]".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        parts.take(2).forEach {
            val decodedString = String(Base64.decode(it.toByteArray(), Base64.URL_SAFE))
            jsonObjects.add(JSONObject(decodedString))
        }
        return jsonObjects
    }
}
