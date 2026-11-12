package my.cardholder.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tombstone of a deleted card, so that the deletion can be propagated by cloud sync */
@Entity(tableName = "deleted_cards")
data class DeletedCard(
    @PrimaryKey
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long,
)
