package my.cardholder.data.model

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

fun SupportedFormat.isSquare(): Boolean {
    return this == SupportedFormat.AZTEC ||
            this == SupportedFormat.DATA_MATRIX ||
            this == SupportedFormat.QR_CODE
}

fun SupportedFormat.getValidCharacters(): String {
    // https://www.activebarcode.com/codes
    return when (this) {
        SupportedFormat.CODABAR ->
            "0123456789-\$:/.+"
        SupportedFormat.CODE_39 ->
            "0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ-.\$/+%"
        SupportedFormat.CODE_93 ->
            "0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ-.\$/+%"
        SupportedFormat.CODE_128 ->
            "0123456789!\"#\$%&''()*+,-./:;<=>?@[\\]^_`{|} ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        SupportedFormat.EAN_8,
        SupportedFormat.EAN_13,
        SupportedFormat.ITF,
        SupportedFormat.UPC_A,
        SupportedFormat.UPC_E ->
            "0123456789"
        else -> "*"
    }
}
