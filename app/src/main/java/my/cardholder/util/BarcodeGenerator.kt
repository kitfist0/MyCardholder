package my.cardholder.util

import android.graphics.Bitmap
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
import java.util.*
import javax.inject.Inject

/**
 * Supported formats (https://zxing.org):
 * UPC-A, UPC-E, EAN-8, EAN-13, Code 39, Code 93, Code 128, ITF, Codabar, R̶S̶S̶-̶1̶4̶, R̶S̶S̶ ̶E̶x̶p̶a̶n̶d̶e̶d̶,
 * QR Code, Data Matrix, Aztec, PDF 417, M̶a̶x̶i̶C̶o̶d̶e̶
 * */
class BarcodeGenerator @Inject constructor() {
    fun generate(
        codeData: String,
        barcodeFormat: BarcodeFormat,
        codeWidth: Int,
        codeHeight: Int,
    ): Bitmap? {
        return try {
            val hintMap = Hashtable<EncodeHintType, ErrorCorrectionLevel>()
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            getWriter(barcodeFormat)?.let { writer ->
                val byteMatrix: BitMatrix = writer.encode(
                    codeData,
                    barcodeFormat,
                    codeWidth,
                    codeHeight,
                    hintMap
                )
                val width = byteMatrix.width
                val height = byteMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for (i in 0 until width) {
                    for (j in 0 until height) {
                        bitmap.setPixel(i, j, if (byteMatrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                bitmap
            }
        } catch (e: WriterException) {
            null
        }
    }

    private fun getWriter(barcodeFormat: BarcodeFormat): Writer? {
        return when (barcodeFormat) {
            BarcodeFormat.UPC_A -> UPCAWriter()
            BarcodeFormat.UPC_E -> UPCEWriter()
            BarcodeFormat.EAN_8 -> EAN8Writer()
            BarcodeFormat.EAN_13 -> EAN13Writer()
            BarcodeFormat.CODE_39 -> Code39Writer()
            BarcodeFormat.CODE_93 -> Code93Writer()
            BarcodeFormat.CODE_128 -> Code128Writer()
            BarcodeFormat.ITF -> ITFWriter()
            BarcodeFormat.CODABAR -> CodaBarWriter()
            BarcodeFormat.QR_CODE -> QRCodeWriter()
            BarcodeFormat.DATA_MATRIX -> DataMatrixWriter()
            BarcodeFormat.AZTEC -> AztecWriter()
            BarcodeFormat.PDF_417 -> PDF417Writer()
            else -> null
        }
    }
}
