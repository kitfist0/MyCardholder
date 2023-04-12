package my.cardholder.ui.scanner.preview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.FragmentScannerPreviewBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class ScannerPreviewFragment : BaseFragment<FragmentScannerPreviewBinding>(
    FragmentScannerPreviewBinding::inflate
) {

    private companion object {
        const val ALPHA_ANIM_DURATION = 300L
    }

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
        collectWhenStarted(viewModel.state) { state ->
            binding.scannerExplanationText.apply {
                if (!state.withExplanation) {
                    animate()
                        .alpha(0f)
                        .setDuration(ALPHA_ANIM_DURATION)
                        .setListener(
                            object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    isVisible = false
                                }
                            }
                        )
                } else {
                    isVisible = true
                }
            }
        }
    }
}
