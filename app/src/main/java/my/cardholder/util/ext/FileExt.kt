package my.cardholder.util.ext

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun File.writeBarcodeBitmap(bitMatrix: BitMatrix) {
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (i in 0 until width) {
        for (j in 0 until height) {
            bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
        }
    }
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val fos = FileOutputStream(this)
    fos.write(bos.toByteArray())
    fos.flush()
    fos.close()
}
