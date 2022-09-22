package my.cardholder.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun File.writeBitmap(bitmap: Bitmap): File? {
    return try {
        createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bos)
        val fos = FileOutputStream(this)
        fos.write(bos.toByteArray())
        fos.flush()
        fos.close()
        this
    } catch (e: IOException) {
        null
    }
}
