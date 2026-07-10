package my.cardholder.data

import com.google.zxing.common.BitMatrix
import io.mockk.*
import my.cardholder.data.model.SupportedFormat
import my.cardholder.util.ext.writeBarcodeBitmap
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BarcodeFileRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var barcodeFileRepository: BarcodeFileRepository

    @Before
    fun setUp() {
        barcodeFileRepository = BarcodeFileRepository(folder.root)
        mockkStatic("my.cardholder.util.ext.FileExtKt")
    }

    @Test
    fun `writeBarcodeFile for square format writes bitmap and returns path`() {
        val content = "test content"
        val format = SupportedFormat.QR_CODE // Square
        
        // Mock the extension function to avoid Android Bitmap dependencies
        every { any<File>().writeBarcodeBitmap(any()) } just Runs

        val resultPath = barcodeFileRepository.writeBarcodeFile(content, format)

        assertNotNull(resultPath)
        assertTrue(resultPath!!.startsWith(folder.root.absolutePath))
        assertTrue(resultPath.endsWith(".jpeg"))
        
        verify { 
            any<File>().writeBarcodeBitmap(match<BitMatrix> { 
                it.width == BarcodeFileRepository.BARCODE_1X1_SIZE && 
                        it.height == BarcodeFileRepository.BARCODE_1X1_SIZE 
            }) 
        }
    }

    @Test
    fun `writeBarcodeFile for non-square format writes bitmap and returns path`() {
        val content = "test content"
        val format = SupportedFormat.CODE_128 // Non-square
        
        every { any<File>().writeBarcodeBitmap(any()) } just Runs

        val resultPath = barcodeFileRepository.writeBarcodeFile(content, format)

        assertNotNull(resultPath)
        
        verify { 
            any<File>().writeBarcodeBitmap(match<BitMatrix> { 
                it.width == BarcodeFileRepository.BARCODE_3X1_WIDTH && 
                        it.height == BarcodeFileRepository.BARCODE_3X1_HEIGHT
            }) 
        }
    }

    @Test
    fun `writeBarcodeFile returns null for invalid content`() {
        val invalidContent = "invalid content for format"
        val format = SupportedFormat.EAN_8 // EAN_8 expects digits
        
        // ZXing will throw an exception for invalid content
        val resultPath = barcodeFileRepository.writeBarcodeFile(invalidContent, format)

        assertTrue(resultPath == null)
    }
}
