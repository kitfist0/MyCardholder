package my.cardholder.util.ext

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun Context.getFileFromExternalDir(fileName: String): File {
    return File(getExternalFilesDir(null), fileName)
}

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
