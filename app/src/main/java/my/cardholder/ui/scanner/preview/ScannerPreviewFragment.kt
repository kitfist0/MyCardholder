package my.cardholder.ui.scanner.preview

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentScannerPreviewBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class ScannerPreviewFragment : BaseFragment<FragmentScannerPreviewBinding>(
    FragmentScannerPreviewBinding::inflate
) {

    override val viewModel: ScannerPreviewViewModel by viewModels()

    override fun initViews() {
        binding.scannerPreview.apply {
            viewModel.bindCamera(
                lifecycleOwner = this@ScannerPreviewFragment,
                surfaceProvider = surfaceProvider,
            )
        }
    }

    override fun collectData() {
    }
}
