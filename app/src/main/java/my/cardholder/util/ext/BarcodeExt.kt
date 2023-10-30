package my.cardholder.util.ext

import com.google.mlkit.vision.barcode.common.Barcode
import my.cardholder.data.model.SupportedFormat

fun Barcode.getContentString(): String {
    return displayValue.toString()
}

fun Barcode.getSupportedFormat(): SupportedFormat? {
    return when (format) {
        Barcode.FORMAT_AZTEC -> SupportedFormat.AZTEC
        Barcode.FORMAT_CODABAR -> SupportedFormat.CODABAR
        Barcode.FORMAT_CODE_39 -> SupportedFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> SupportedFormat.CODE_93
        Barcode.FORMAT_CODE_128 -> SupportedFormat.CODE_128
        Barcode.FORMAT_DATA_MATRIX -> SupportedFormat.DATA_MATRIX
        Barcode.FORMAT_EAN_8 -> SupportedFormat.EAN_8
        Barcode.FORMAT_EAN_13 -> SupportedFormat.EAN_13
        Barcode.FORMAT_ITF -> SupportedFormat.ITF
        Barcode.FORMAT_PDF417 -> SupportedFormat.PDF_417
        Barcode.FORMAT_QR_CODE -> SupportedFormat.QR_CODE
        Barcode.FORMAT_UPC_A -> SupportedFormat.UPC_A
        Barcode.FORMAT_UPC_E -> SupportedFormat.UPC_E
        else -> null
    }
}
