package my.cardholder.ui.scanner.preview

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.FragmentScannerPreviewBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class ScannerPreviewFragment : BaseFragment<FragmentScannerPreviewBinding>(
    FragmentScannerPreviewBinding::inflate
) {

    override val viewModel: ScannerPreviewViewModel by viewModels()

    override fun initViews() {
        lifecycleScope.launch {
            viewModel.startCamera(
                lifecycleOwner = this@ScannerPreviewFragment,
                surfaceProvider = binding.scannerPreview.surfaceProvider,
            )
        }
    }

    override fun collectData() {
    }
}
