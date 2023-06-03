package my.cardholder.data

import com.google.zxing.BarcodeFormat
import com.google.zxing.Writer
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.CodaBarWriter
import com.google.zxing.oned.Code128Writer
import com.google.zxing.oned.Code39Writer
import com.google.zxing.oned.Code93Writer
import com.google.zxing.oned.EAN13Writer
import com.google.zxing.oned.EAN8Writer
import com.google.zxing.oned.ITFWriter
import com.google.zxing.oned.UPCAWriter
import com.google.zxing.oned.UPCEWriter
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.qrcode.QRCodeWriter
import my.cardholder.data.model.BarcodeFilePath
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.isSquare
import my.cardholder.util.ext.writeBarcodeBitmap
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeFileRepository @Inject constructor(
    private val filesDir: File,
) {

    private companion object {
        const val BARCODE_1X1_SIZE = 650
        const val BARCODE_3X1_HEIGHT = 325
        const val BARCODE_3X1_WIDTH = 975
    }

    fun writeBarcodeFile(content: String, supportedFormat: SupportedFormat): BarcodeFilePath? {
        val barcodeFile = File(filesDir, "${UUID.randomUUID()}.jpeg")
        return runCatching {
            val isSquare = supportedFormat.isSquare()
            val bitMatrix = getWriter(supportedFormat).encode(
                content,
                BarcodeFormat.valueOf(supportedFormat.toString()),
                if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_WIDTH,
                if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_HEIGHT,
            )
            barcodeFile.writeBarcodeBitmap(bitMatrix)
            barcodeFile.absolutePath
        }.getOrNull()
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
}
