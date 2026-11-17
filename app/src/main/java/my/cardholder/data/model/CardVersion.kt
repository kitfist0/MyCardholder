package my.cardholder.data.model

import androidx.room.ColumnInfo

data class CardVersion(
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "changed_at")
    val changedAt: Long,
    @ColumnInfo(name = "position_changed_at")
    val positionChangedAt: Long,
    @ColumnInfo(name = "category_changed_at")
    val categoryChangedAt: Long,
)
