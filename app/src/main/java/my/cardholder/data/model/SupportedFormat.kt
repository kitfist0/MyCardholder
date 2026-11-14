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

fun SupportedFormat.isTwoDimensional(): Boolean {
    return isSquare() || this == SupportedFormat.PDF_417
}

fun SupportedFormat.toSpec(): FormatSpec {
    return when (this) {
        SupportedFormat.AZTEC ->
            FormatSpec(this, "Aztec", FormatCharacters.ASCII_255)
        SupportedFormat.CODABAR ->
            FormatSpec(this, "Codabar", FormatCharacters.CODABAR)
        SupportedFormat.CODE_39 ->
            FormatSpec(this, "Code 39", FormatCharacters.ALPHANUMERIC)
        SupportedFormat.CODE_93 ->
            FormatSpec(this, "Code 93", FormatCharacters.ALPHANUMERIC)
        SupportedFormat.CODE_128 ->
            FormatSpec(this, "Code 128", FormatCharacters.ASCII_128)
        SupportedFormat.DATA_MATRIX ->
            FormatSpec(this, "DataMatrix", FormatCharacters.ASCII_255)
        SupportedFormat.EAN_8 ->
            FormatSpec(this, "EAN-8", FormatCharacters.DIGITS, FormatLength.Fixed(8))
        SupportedFormat.EAN_13 ->
            FormatSpec(this, "EAN-13", FormatCharacters.DIGITS, FormatLength.Fixed(13))
        SupportedFormat.ITF ->
            FormatSpec(this, "ITF", FormatCharacters.DIGITS, FormatLength.Even)
        SupportedFormat.PDF_417 ->
            FormatSpec(this, "PDF417", FormatCharacters.ASCII_255)
        SupportedFormat.QR_CODE ->
            FormatSpec(this, "QR code", FormatCharacters.ASCII_255)
        SupportedFormat.UPC_A ->
            FormatSpec(this, "UPC-A", FormatCharacters.DIGITS, FormatLength.Fixed(12))
        SupportedFormat.UPC_E ->
            FormatSpec(this, "UPC-E", FormatCharacters.DIGITS, FormatLength.Fixed(8))
    }
}
