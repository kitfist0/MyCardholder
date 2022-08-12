package my.cardholder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val text: String,
    val logo: String = "",
    val color: String,
    val format: String,
    val time: Long,
) {
    companion object {
        fun Card.nameTransitionId() = "trans_name_$id"
        fun Card.textTransitionId() = "trans_text_$id"
        fun Card.barcodeTransitionId() = "trans_barcode_$id"
    }
}
