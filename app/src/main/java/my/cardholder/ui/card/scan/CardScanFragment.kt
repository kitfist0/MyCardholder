package my.cardholder.ui.card.scan

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
import javax.inject.Inject

@AndroidEntryPoint
class CardScanFragment : BaseFragment<FragmentCardScanBinding>(
    FragmentCardScanBinding::inflate
) {

    @Inject
    lateinit var imageAnalysis: ImageAnalysis

    private var cameraProvider: ProcessCameraProvider? = null

    private val fileSelectionRequest = registerForActivityResult(PickVisualMedia()) { uri ->
        viewModel.onFileSelectionRequestResult(uri)
    }

    override val viewModel: CardScanViewModel by viewModels()

    override fun initViews() {
        binding.cardScanSelectFileFab.setOnClickListener {
            viewModel.onSelectFileFabClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cardScanPreview.surfaceProvider)
            cameraProvider?.unbindAll()
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
            if (state.launchFileSelectionRequest) {
                fileSelectionRequest.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                viewModel.onFileSelectionRequestLaunched()
            }
        }
    }
}
