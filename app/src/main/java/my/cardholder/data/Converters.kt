package my.cardholder.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun listToString(value: List<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToList(string: String?): List<String>? {
        return string?.split(",")
    }
}
