package my.cardholder.util.ext

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun File.writeBitmap(bitmap: Bitmap): File? {
    val result = runCatching {
        createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val fos = FileOutputStream(this)
        fos.write(bos.toByteArray())
        fos.flush()
        fos.close()
        this
    }
    return result.getOrNull()
}
