package my.wallet.ui.scanner

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.wallet.databinding.FragmentScannerBinding

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    val scannerViewModel: ScannerViewModel by viewModels()

    private var _binding: FragmentScannerBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.scannerPreview.apply {
            scannerViewModel.bindCamera(
                lifecycleOwner = this@ScannerFragment,
                surfaceProvider = surfaceProvider,
                targetSize = Size(width, height),
            )
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
