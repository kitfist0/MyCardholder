package my.cardholder.ui.card.scan

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.FragmentCardScanBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CardScanFragment : BaseFragment<FragmentCardScanBinding>(
    FragmentCardScanBinding::inflate
), ImageAnalysis.Analyzer {

    @Inject
    lateinit var preview: Preview

    @Inject
    lateinit var imageAnalysis: ImageAnalysis

    @Inject
    lateinit var selector: CameraSelector

    private var cameraProvider: ProcessCameraProvider? = null

    private val barcodeFileSelectionRequest = registerForActivityResult(PickVisualMedia()) { uri ->
        viewModel.onBarcodeFileSelectionRequestResult(uri?.toString())
    }

    override val viewModel: CardScanViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            cardScanPreliminaryResultButton.setOnClickListener {
                viewModel.onPreliminaryResultButtonClicked()
            }
            cardScanSelectFileFab.setOnClickListener {
                viewModel.onSelectFileFabClicked()
            }
            cardScanAddManuallyFab.setOnClickListener {
                viewModel.onAddManuallyFabClicked()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
            cameraProvider?.unbindAll()
            preview.setSurfaceProvider(binding.cardScanPreview.surfaceProvider)
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), this@CardScanFragment)
            cameraProvider?.bindToLifecycle(this@CardScanFragment, selector, preview, imageAnalysis)
        }
    }

    override fun onPause() {
        imageAnalysis.clearAnalyzer()
        cameraProvider?.unbindAll()
        super.onPause()
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.cardScanExplanationMessageText.isVisible = state.withExplanation
            binding.cardScanPreliminaryResultButton.text = state.preliminaryScanResult?.content
            binding.cardScanPreliminaryResultButton.isVisible = state.preliminaryScanResult != null
            if (state.launchBarcodeFileSelectionRequest) {
                barcodeFileSelectionRequest.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                viewModel.onBarcodeFileSelectionRequestLaunched()
            }
        }
    }

    override fun analyze(image: ImageProxy) {
        viewModel.onNewCameraImage(image)
    }
}
