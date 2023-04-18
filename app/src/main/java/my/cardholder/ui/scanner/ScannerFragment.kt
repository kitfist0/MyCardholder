package my.cardholder.ui.scanner

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
class ScannerFragment : BaseFragment<FragmentScannerPreviewBinding>(
    FragmentScannerPreviewBinding::inflate
) {

    private companion object {
        const val ALPHA_ANIM_DURATION = 300L
    }

    override val viewModel: ScannerViewModel by viewModels()

    override fun initViews() {
        lifecycleScope.launch {
            viewModel.startCamera(
                lifecycleOwner = this@ScannerFragment,
                surfaceProvider = binding.scannerPreview.surfaceProvider,
            )
        }
        binding.scannerAddManuallyFab.setOnClickListener {
            viewModel.onAddManuallyFabClicked()
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
