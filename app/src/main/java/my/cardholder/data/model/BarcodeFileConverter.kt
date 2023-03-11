package my.cardholder.data.model

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.io.File

@ProvidedTypeConverter
class BarcodeFileConverter(private val filesDir: File) {
    @TypeConverter
    fun stringToBarcodeFile(string: String?): File? {
        return string?.let { File(filesDir, it) }
    }

    @TypeConverter
    fun barcodeFileToString(file: File?): String? {
        return file?.name
    }
}
