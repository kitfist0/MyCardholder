package my.cardholder.ui.card.zoom

import android.transition.TransitionInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardZoomBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.loadBarcodeImage
import my.cardholder.util.ext.setupUniqueTransitionName

@AndroidEntryPoint
class CardZoomFragment : BaseFragment<FragmentCardZoomBinding>(
    FragmentCardZoomBinding::inflate
) {

    private val args: CardZoomFragmentArgs by navArgs()

    override val viewModel: CardZoomViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardZoomBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
        }
        setMaxScreenBrightness()
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardZoomState.Loading ->
                    binding.cardZoomBarcodeImage.isVisible = false
                is CardZoomState.Success ->
                    binding.cardZoomBarcodeImage.apply {
                        isVisible = true
                        setBackgroundColor(state.cardColor)
                        loadBarcodeImage(state.barcodeFile)
                    }
            }
        }
    }

    private fun setMaxScreenBrightness() {
        (activity as? AppCompatActivity)?.let {
            val attributes = it.window.attributes
            attributes.screenBrightness = 1f
            it.window.attributes = attributes
        }
    }
}
