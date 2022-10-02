package my.cardholder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.SupportedFormat
import my.cardholder.util.writeBitmap
import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val context: Context,
) {

    companion object {
        private const val BARCODE_1X1_SIZE = 700
        private const val BARCODE_3X1_HEIGHT = 300
        private const val BARCODE_3X1_WIDTH = 900
        private const val CARD_NAME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    val cards: Flow<List<Card>> = cardDao.getCards()

    suspend fun getCard(cardId: Long): Card {
        return cardDao.getCard(cardId)
    }

    suspend fun insertCard(
        text: String,
        timestamp: Long,
        supportedFormat: SupportedFormat,
    ): Long {
        val card = Card(
            name = "Card ${SimpleDateFormat(CARD_NAME_FORMAT).format(timestamp)}",
            text = text,
            color = "",
            timestamp = timestamp,
            format = supportedFormat,
        )
        writeBarcodeFile(
            file = card.getBarcodeFile(context),
            codeData = text,
            codeFormat = supportedFormat,
        )
        return cardDao.insert(card)
    }

    suspend fun deleteCard(cardId: Long) {
        val card = getCard(cardId)
        card.getBarcodeFile(context).delete()
        cardDao.deleteCard(card.id)
    }

    private fun writeBarcodeFile(
        file: File,
        codeData: String,
        codeFormat: SupportedFormat,
    ): File? {
        return try {
            val bitMatrix = getWriter(codeFormat)
                .encode(codeData, codeFormat)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (i in 0 until width) {
                for (j in 0 until height) {
                    bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }
            file.writeBitmap(bitmap)
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

    private fun Writer.encode(
        codeData: String,
        codeFormat: SupportedFormat,
    ): BitMatrix {
        val isSquare = codeFormat == SupportedFormat.AZTEC ||
                codeFormat == SupportedFormat.DATA_MATRIX ||
                codeFormat == SupportedFormat.QR_CODE
        return encode(
            codeData,
            BarcodeFormat.valueOf(codeFormat.toString()),
            if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_WIDTH,
            if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_HEIGHT,
        )
    }
}