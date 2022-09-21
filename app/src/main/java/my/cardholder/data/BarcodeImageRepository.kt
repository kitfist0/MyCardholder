package my.cardholder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeImageRepository @Inject constructor(
    private val context: Context,
) {

    companion object {
        private const val BARCODE_HEIGHT = 300
        private const val BARCODE_WIDTH = 600
        private const val BARCODE_SIZE = 500
    }

    fun getBarcodeBitmap(
        cardId: Long,
        codeData: String,
        codeFormat: SupportedFormat,
    ): Bitmap? {
        val fileName = "${cardId}_$codeFormat.jpeg"
        val file = getImageFile(fileName)
        return if (file.exists() && file.canRead()) {
            BitmapFactory.decodeFile(file.path)
        } else {
            saveBitmap(codeData, codeFormat, fileName)
        }
    }

    private fun saveBitmap(
        codeData: String,
        codeFormat: SupportedFormat,
        fileName: String,
    ): Bitmap? {
        return try {
            val hintMap = Hashtable<EncodeHintType, ErrorCorrectionLevel>()
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            val writer = getWriter(codeFormat)
            val isSquare = codeFormat == SupportedFormat.QR_CODE ||
                    codeFormat == SupportedFormat.DATA_MATRIX
            val bitMatrix: BitMatrix = writer.encode(
                codeData,
                BarcodeFormat.valueOf(codeFormat.toString()),
                if (isSquare) BARCODE_SIZE else BARCODE_WIDTH,
                if (isSquare) BARCODE_SIZE else BARCODE_HEIGHT,
                hintMap
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (i in 0 until width) {
                for (j in 0 until height) {
                    bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap?.writeToFile(fileName)
            bitmap
        } catch (e: WriterException) {
            null
        }
    }

    private fun getWriter(supportedFormat: SupportedFormat): Writer {
        return when (supportedFormat) {
            SupportedFormat.UPC_A -> UPCAWriter()
            SupportedFormat.UPC_E -> UPCEWriter()
            SupportedFormat.EAN_8 -> EAN8Writer()
            SupportedFormat.EAN_13 -> EAN13Writer()
            SupportedFormat.CODE_39 -> Code39Writer()
            SupportedFormat.CODE_93 -> Code93Writer()
            SupportedFormat.CODE_128 -> Code128Writer()
            SupportedFormat.ITF -> ITFWriter()
            SupportedFormat.CODABAR -> CodaBarWriter()
            SupportedFormat.QR_CODE -> QRCodeWriter()
            SupportedFormat.DATA_MATRIX -> DataMatrixWriter()
            SupportedFormat.AZTEC -> AztecWriter()
            SupportedFormat.PDF_417 -> PDF417Writer()
        }
    }

    private fun Bitmap.writeToFile(fileName: String): File? {
        val file = getImageFile(fileName)
        return try {
            file.createNewFile()
            val bos = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.JPEG, 0, bos)
            val fos = FileOutputStream(file)
            fos.write(bos.toByteArray())
            fos.flush()
            fos.close()
            file
        } catch (e: IOException) {
            null
        }
    }

    private fun getImageFile(fileName: String): File {
        return File(context.getExternalFilesDir("images"), fileName)
    }
}
