package my.cardholder.data

import app.cash.turbine.test
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.ScanResult
import my.cardholder.data.model.SupportedFormat
import my.cardholder.util.ImageFileDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanResultRepositoryTest {

    private val barcodeScanner = mockk<BarcodeScanner>()
    private lateinit var scanResultRepository: ScanResultRepository

    private val imageFileDecoder = mockk<ImageFileDecoder>()
    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val inputImage = mockk<InputImage>()

    @Before
    fun setUp() {
        // File scans wrap the decoded bitmap into an InputImage, which needs the Android runtime.
        mockkStatic(InputImage::class)
        every { imageFileDecoder.decodeUprightBitmap(IMAGE_URI) } returns bitmap
        every { InputImage.fromBitmap(bitmap, 0) } returns inputImage
        scanResultRepository = ScanResultRepository(barcodeScanner, imageFileDecoder)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * Успешное сканирование:
     * проверяется корректная конвертация формата ML Kit (Barcode.FORMAT_QR_CODE) во внутренний
     * формат приложения (SupportedFormat.QR_CODE) и извлечение текстового значения
     **/
    @Test
    fun `scan file image success`() = runTest {
        val barcode = mockk<Barcode>()
        val barcodes = listOf(barcode)
        val task = mockk<Task<List<Barcode>>>()

        every { barcode.format } returns Barcode.FORMAT_QR_CODE
        every { barcode.displayValue } returns "test content"
        every { barcode.boundingBox } returns null
        every { barcodeScanner.process(inputImage) } returns task

        val successSlot = slot<OnSuccessListener<List<Barcode>>>()
        every { task.addOnSuccessListener(capture(successSlot)) } returns task
        every { task.addOnFailureListener(any()) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(IMAGE_URI)
            successSlot.captured.onSuccess(barcodes)

            val result = awaitItem()
            assertTrue(result is ScanResult.Success)
            assertEquals("test content", (result as ScanResult.Success).content)
            assertEquals(SupportedFormat.QR_CODE, result.format)
        }
    }

    /**
     * Обработка ошибок:
     * проверяется, что исключения от ML Kit пробрасываются в поток результатов как
     * ScanResult.Failure
     **/
    @Test
    fun `scan file image failure`() = runTest {
        val task = mockk<Task<List<Barcode>>>()
        val exception = Exception("Scan failed")

        every { barcodeScanner.process(inputImage) } returns task

        val failureSlot = slot<OnFailureListener>()
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(capture(failureSlot)) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(IMAGE_URI)
            failureSlot.captured.onFailure(exception)

            val result = awaitItem()
            assertTrue(result is ScanResult.Failure)
            assertEquals(exception, (result as ScanResult.Failure).throwable)
        }
    }

    /**
     * Пустой результат:
     * проверяется сценарий, когда камера ничего не распознала (возвращается ScanResult.Nothing)
     */
    /**
     * Нечитаемый файл:
     * если изображение не удалось декодировать, в поток результатов приходит ScanResult.Failure
     */
    @Test
    fun `scan file image that cannot be decoded`() = runTest {
        every { imageFileDecoder.decodeUprightBitmap(IMAGE_URI) } returns null

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(IMAGE_URI)

            assertTrue(awaitItem() is ScanResult.Failure)
        }
        verify(exactly = 0) { barcodeScanner.process(any<InputImage>()) }
    }

    @Test
    fun `scan file image nothing found`() = runTest {
        val task = mockk<Task<List<Barcode>>>()

        every { barcodeScanner.process(inputImage) } returns task

        val successSlot = slot<OnSuccessListener<List<Barcode>>>()
        every { task.addOnSuccessListener(capture(successSlot)) } returns task
        every { task.addOnFailureListener(any()) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(IMAGE_URI)
            successSlot.captured.onSuccess(emptyList())

            val result = awaitItem()
            assertTrue(result is ScanResult.Nothing)
        }
    }

    private companion object {
        const val IMAGE_URI = "content://media/picker/0/image/1"
    }
}
