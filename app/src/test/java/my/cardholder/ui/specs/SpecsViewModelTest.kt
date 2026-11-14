package my.cardholder.ui.specs

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import my.cardholder.R
import my.cardholder.data.model.FormatCharacters
import my.cardholder.data.model.FormatLength
import my.cardholder.data.model.SupportedFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class SpecsViewModelTest {

    @Test
    fun `state groups linear formats before two dimensional formats`() = runTest {
        val viewModel = SpecsViewModel()

        viewModel.state.test {
            val items = awaitItem().items
            val expected = listOf(
                SpecsItem.Header(R.string.specs_group_linear),
                SupportedFormat.CODABAR,
                SupportedFormat.CODE_39,
                SupportedFormat.CODE_93,
                SupportedFormat.CODE_128,
                SupportedFormat.EAN_8,
                SupportedFormat.EAN_13,
                SupportedFormat.ITF,
                SupportedFormat.UPC_A,
                SupportedFormat.UPC_E,
                SpecsItem.Header(R.string.specs_group_two_dimensional),
                SupportedFormat.AZTEC,
                SupportedFormat.DATA_MATRIX,
                SupportedFormat.PDF_417,
                SupportedFormat.QR_CODE,
            )
            val actual = items.map { item ->
                when (item) {
                    is SpecsItem.Header -> item
                    is SpecsItem.Format -> item.spec.format
                }
            }
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `state contains every supported format once`() = runTest {
        val viewModel = SpecsViewModel()

        viewModel.state.test {
            val formats = awaitItem().items
                .filterIsInstance<SpecsItem.Format>()
                .map { it.spec.format }
            assertEquals(SupportedFormat.entries.toSet(), formats.toSet())
            assertEquals(SupportedFormat.entries.size, formats.size)
        }
    }

    @Test
    fun `state describes characters and length of each format`() = runTest {
        val viewModel = SpecsViewModel()

        viewModel.state.test {
            val specs = awaitItem().items
                .filterIsInstance<SpecsItem.Format>()
                .associate { it.spec.format to it.spec }

            val ean13 = specs.getValue(SupportedFormat.EAN_13)
            assertEquals(FormatCharacters.DIGITS, ean13.characters)
            assertEquals(FormatLength.Fixed(13), ean13.length)

            val itf = specs.getValue(SupportedFormat.ITF)
            assertEquals(FormatLength.Even, itf.length)

            val qrCode = specs.getValue(SupportedFormat.QR_CODE)
            assertEquals(FormatCharacters.ASCII_255, qrCode.characters)
            assertEquals(FormatLength.Unfixed, qrCode.length)
        }
    }
}
