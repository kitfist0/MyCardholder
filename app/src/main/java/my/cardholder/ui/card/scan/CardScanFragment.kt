package my.cardholder.ui.card.scan

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardScanBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardScanFragment : BaseFragment<FragmentCardScanBinding>(
    FragmentCardScanBinding::inflate
) {

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
        viewModel.bindCamera(
            lifecycleOwner = this@CardScanFragment,
            surfaceProvider = binding.cardScanPreview.surfaceProvider,
        )
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
