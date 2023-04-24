package my.cardholder.ui.card.scan

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.FragmentCardScanBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardScanFragment : BaseFragment<FragmentCardScanBinding>(
    FragmentCardScanBinding::inflate
) {

    private companion object {
        const val ALPHA_ANIM_DURATION = 300L
    }

    override val viewModel: CardScanViewModel by viewModels()

    override fun initViews() {
        lifecycleScope.launch {
            viewModel.startCamera(
                lifecycleOwner = this@CardScanFragment,
                surfaceProvider = binding.cardScanPreview.surfaceProvider,
            )
        }
        binding.cardScanAddManuallyFab.setOnClickListener {
            viewModel.onAddManuallyFabClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.cardScanExplanationMessageText.apply {
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
