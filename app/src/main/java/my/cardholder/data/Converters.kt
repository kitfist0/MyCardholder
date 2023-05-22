package my.cardholder.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun listToString(value: List<Long>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToList(string: String?): List<Long>? {
        return string?.split(",")
            ?.map { it.toLong() }
    }
}
