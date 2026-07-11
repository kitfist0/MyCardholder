package my.cardholder.data

import app.cash.turbine.test
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.ScanResult
import my.cardholder.data.model.SupportedFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanResultRepositoryTest {

    private val barcodeScanner = mockk<BarcodeScanner>()
    private lateinit var scanResultRepository: ScanResultRepository

    @Before
    fun setUp() {
        scanResultRepository = ScanResultRepository(barcodeScanner)
    }

    /**
     * Успешное сканирование:
     * проверяется корректная конвертация формата ML Kit (Barcode.FORMAT_QR_CODE) во внутренний
     * формат приложения (SupportedFormat.QR_CODE) и извлечение текстового значения
     **/
    @Test
    fun `scan InputImage success`() = runTest {
        val inputImage = mockk<InputImage>()
        val barcode = mockk<Barcode>()
        val barcodes = listOf(barcode)
        val task = mockk<Task<List<Barcode>>>()

        every { barcode.format } returns Barcode.FORMAT_QR_CODE
        every { barcode.displayValue } returns "test content"
        every { barcodeScanner.process(inputImage) } returns task

        val successSlot = slot<OnSuccessListener<List<Barcode>>>()
        every { task.addOnSuccessListener(capture(successSlot)) } returns task
        every { task.addOnFailureListener(any()) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(inputImage)
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
    fun `scan InputImage failure`() = runTest {
        val inputImage = mockk<InputImage>()
        val task = mockk<Task<List<Barcode>>>()
        val exception = Exception("Scan failed")

        every { barcodeScanner.process(inputImage) } returns task

        val failureSlot = slot<OnFailureListener>()
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(capture(failureSlot)) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(inputImage)
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
    @Test
    fun `scan InputImage nothing found`() = runTest {
        val inputImage = mockk<InputImage>()
        val task = mockk<Task<List<Barcode>>>()

        every { barcodeScanner.process(inputImage) } returns task

        val successSlot = slot<OnSuccessListener<List<Barcode>>>()
        every { task.addOnSuccessListener(capture(successSlot)) } returns task
        every { task.addOnFailureListener(any()) } returns task

        scanResultRepository.fileScanResult.test {
            scanResultRepository.scan(inputImage)
            successSlot.captured.onSuccess(emptyList())

            val result = awaitItem()
            assertTrue(result is ScanResult.Nothing)
        }
    }
}
