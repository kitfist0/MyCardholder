package my.cardholder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.zxing.BarcodeFormat
import com.google.zxing.Writer
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.isSquare
import my.cardholder.util.ext.getFileFromExternalDir
import my.cardholder.util.ext.writeBitmap
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val context: Context,
) {

    private companion object {
        const val BARCODE_1X1_SIZE = 650
        const val BARCODE_3X1_HEIGHT = 325
        const val BARCODE_3X1_WIDTH = 975
        const val CARD_NAME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        const val CSV_SCHEME_VERSION = 1
    }

    val cards: Flow<List<Card>> = cardDao.getCards()

    fun getCard(cardId: Long): Flow<Card?> {
        return cardDao.getCard(cardId)
    }

    suspend fun insertCard(text: String, supportedFormat: SupportedFormat): Long {
        val timestamp = System.currentTimeMillis()
        val card = Card(
            name = "Card ${SimpleDateFormat(CARD_NAME_FORMAT, Locale.US).format(timestamp)}",
            text = text,
            timestamp = timestamp,
            format = supportedFormat,
        )
        card.writeNewBarcodeFile()
        return cardDao.insert(card)
    }

    suspend fun searchForCardsWithNamesLike(name: String): List<Card> {
        return if (name.isNotBlank()) {
            cardDao.getCardsWithNamesLike("%$name%")
        } else {
            emptyList()
        }
    }

    suspend fun updateCardColor(cardId: Long, color: String) {
        val oldCard = getCard(cardId).first()
        oldCard?.copy(color = color)
            ?.let { newCard -> cardDao.update(newCard) }
    }

    suspend fun updateCardDataIfItChanges(cardId: Long, name: String?, text: String?) {
        val oldCard = getCard(cardId).first() ?: return
        val oldCardName = oldCard.name
        val oldCardText = oldCard.text
        if (oldCardName == name && oldCardText == text) {
            return
        }
        val newCard = if (oldCardText != text) {
            oldCard.deleteBarcodeFile()
            oldCard.copy(
                name = name ?: oldCardName,
                text = text ?: oldCardText,
                timestamp = System.currentTimeMillis(),
            ).also { card -> card.writeNewBarcodeFile() }
        } else {
            oldCard.copy(
                name = name ?: oldCardName,
                text = text,
            )
        }
        cardDao.update(newCard)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId).first()?.let { card ->
            card.deleteBarcodeFile()
            cardDao.deleteCard(card.id)
        }
    }

    suspend fun exportCards(outputStream: OutputStream) {
        csvWriter().openAsync(outputStream) {
            writeRow(CSV_SCHEME_VERSION)
            cardDao.getCards().first().forEach { card ->
                writeRow(listOf(card.name, card.text, card.color, card.timestamp, card.format))
            }
        }
    }

    suspend fun importCards(inputStream: InputStream) {
        val reader = csvReader {
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }
        reader.openAsync(inputStream) {
            val version = readLine()?.toInt()
            if (version == 1) {
                readAllAsSequence(fieldsNum = 5).forEach { row ->
                    val card = Card(
                        name = row[0],
                        text = row[1],
                        color = row[2],
                        timestamp = row[3].toLong(),
                        format = SupportedFormat.valueOf(row[4]),
                    )
                    cardDao.insert(card)
                    card.writeNewBarcodeFile()
                }
            }
        }
    }

    private fun Card.getBarcodeFile(): File {
        return context.getFileFromExternalDir(barcodeFileName)
    }

    private fun Card.deleteBarcodeFile() {
        getBarcodeFile().delete()
    }

    private fun Card.writeNewBarcodeFile() {
        val isSquare = format.isSquare()
        runCatching {
            val bitMatrix = getWriter(format).encode(
                text,
                BarcodeFormat.valueOf(format.toString()),
                if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_WIDTH,
                if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_HEIGHT,
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (i in 0 until width) {
                for (j in 0 until height) {
                    bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }
            getBarcodeFile().writeBitmap(bitmap)
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
}
