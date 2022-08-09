package my.cardholder.ui.scanner

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentScannerBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class ScannerFragment : BaseFragment<FragmentScannerBinding>(FragmentScannerBinding::inflate) {

    override val viewModel: ScannerViewModel by viewModels()

    override fun initViews() {
        binding.scannerPreview.apply {
            viewModel.bindCamera(
                lifecycleOwner = this@ScannerFragment,
                surfaceProvider = surfaceProvider,
            )
        }
    }

    override fun collectData() {
    }
}
