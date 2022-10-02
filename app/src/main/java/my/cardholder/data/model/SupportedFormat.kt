package my.cardholder.data.model

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Supported barcode formats (https://zxing.org):
 * UPC-A, UPC-E, EAN-8, EAN-13, Code 39, Code 93, Code 128, ITF, Codabar, R̶S̶S̶-̶1̶4̶, R̶S̶S̶ ̶E̶x̶p̶a̶n̶d̶e̶d̶,
 * QR Code, Data Matrix, Aztec, PDF 417, M̶a̶x̶i̶C̶o̶d̶e̶
 * */
enum class SupportedFormat {
    AZTEC,
    CODABAR,
    CODE_39,
    CODE_93,
    CODE_128,
    DATA_MATRIX,
    EAN_8,
    EAN_13,
    ITF,
    PDF_417,
    QR_CODE,
    UPC_A,
    UPC_E,
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
