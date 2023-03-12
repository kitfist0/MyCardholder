package my.cardholder.data

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
import my.cardholder.util.ext.writeBarcodeBitmap
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val filesDir: File,
) {

    private companion object {
        const val BARCODE_1X1_SIZE = 650
        const val BARCODE_3X1_HEIGHT = 325
        const val BARCODE_3X1_WIDTH = 975
        const val CARD_NAME_FORMAT = "MMMdd HH:mm:ss"
        const val CSV_SCHEME_VERSION = 1
    }

    val cards: Flow<List<Card>> = cardDao.getCards()

    fun getCard(cardId: Long): Flow<Card?> {
        return cardDao.getCard(cardId)
    }

    suspend fun insertCard(content: String, supportedFormat: SupportedFormat): Long {
        val timestamp = System.currentTimeMillis()
        val card = createNewCardAndBarcodeFile(
            name = SimpleDateFormat(CARD_NAME_FORMAT, Locale.US).format(timestamp),
            content = content,
            format = supportedFormat,
        )
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

    suspend fun updateCardDataIfItChanges(
        id: Long,
        name: String?,
        content: String?,
        format: SupportedFormat?,
    ) {
        val oldCard = getCard(id).first() ?: return
        val oldCardName = oldCard.name
        val oldCardContent = oldCard.content
        val oldCardFormat = oldCard.format
        if (oldCardName == name && oldCardContent == content && oldCardFormat == format) {
            return
        }
        val newCard = if (oldCardContent != content || oldCardFormat != format) {
            oldCard.barcodeFile?.delete()
            createNewCardAndBarcodeFile(
                id = oldCard.id,
                name = name ?: oldCardName,
                content = content ?: oldCardContent,
                color = oldCard.color,
                format = format ?: oldCardFormat,
            )
        } else {
            oldCard.copy(
                name = name ?: oldCardName,
                content = content,
            )
        }
        cardDao.update(newCard)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId).first()?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCard(card.id)
        }
    }

    suspend fun exportCards(outputStream: OutputStream): Result<Boolean> {
        return runCatching {
            csvWriter().openAsync(outputStream) {
                writeRow(CSV_SCHEME_VERSION)
                cardDao.getCards().first().reversed().forEach { card ->
                    writeRow(listOf(card.name, card.content, card.color, card.format))
                }
            }
            true
        }
    }

    suspend fun importCards(inputStream: InputStream): Result<Boolean> {
        val reader = csvReader {
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }
        return runCatching {
            reader.openAsync(inputStream) {
                val version = readNext()?.first()?.toInt()
                if (version == 1) {
                    readAllAsSequence(fieldsNum = 4).forEach { row ->
                        val card = createNewCardAndBarcodeFile(
                            name = row[0],
                            content = row[1],
                            color = row[2],
                            format = SupportedFormat.valueOf(row[3]),
                        )
                        cardDao.insert(card)
                    }
                }
            }
            true
        }
    }

    private fun createNewCardAndBarcodeFile(
        id: Long? = null,
        name: String,
        content: String,
        color: String? = null,
        format: SupportedFormat,
    ): Card {
        return Card(
            id = id ?: 0,
            name = name,
            content = content,
            color = color ?: Card.COLORS.random(),
            format = format,
            barcodeFile = File(filesDir, "${UUID.randomUUID()}.jpeg"),
        ).also { card ->
            runCatching {
                val isSquare = card.format.isSquare()
                val bitMatrix = getWriter(format).encode(
                    content,
                    BarcodeFormat.valueOf(format.toString()),
                    if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_WIDTH,
                    if (isSquare) BARCODE_1X1_SIZE else BARCODE_3X1_HEIGHT,
                )
                card.barcodeFile?.writeBarcodeBitmap(bitMatrix)
            }
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
