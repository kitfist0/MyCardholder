package my.cardholder.data.model

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

fun SupportedFormat.toSpec(): FormatSpec {
    return when (this) {
        SupportedFormat.AZTEC ->
            FormatSpec("Aztec", "ASCII 1-255")
        SupportedFormat.CODABAR ->
            FormatSpec("Codabar", "0123456789-\$:/.+")
        SupportedFormat.CODE_39 ->
            FormatSpec("Code 39", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -.\$/+%")
        SupportedFormat.CODE_93 ->
            FormatSpec("Code 93", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -.\$/+%")
        SupportedFormat.CODE_128 ->
            FormatSpec("Code 128", "ASCII 1-128")
        SupportedFormat.DATA_MATRIX ->
            FormatSpec("DataMatrix", "ASCII 1-255")
        SupportedFormat.EAN_8 ->
            FormatSpec("EAN-8", "0123456789", "8")
        SupportedFormat.EAN_13 ->
            FormatSpec("EAN-13", "0123456789", "13")
        SupportedFormat.ITF ->
            FormatSpec("ITF", "0123456789", "even value")
        SupportedFormat.PDF_417 ->
            FormatSpec("PDF417","ASCII 1-255")
        SupportedFormat.QR_CODE ->
            FormatSpec("QR code", "ASCII 1-255")
        SupportedFormat.UPC_A ->
            FormatSpec("UPC-A", "0123456789", "12")
        SupportedFormat.UPC_E ->
            FormatSpec("UPC-E", "0123456789", "8")
    }
}
