package my.cardholder.ui.card.zoom

import android.os.Bundle
import android.provider.Settings
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var prevBrightness = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        prevBrightness = Settings.System.getInt(
            context?.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            50
        ) / 255f
        setScreenBrightness(1f)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        setScreenBrightness(prevBrightness)
        super.onDestroyView()
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardZoomBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
        }
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

    private fun setScreenBrightness(brightness: Float) {
        (activity as? AppCompatActivity)?.let {
            val attributes = it.window.attributes
            attributes.screenBrightness = brightness
            it.window.attributes = attributes
        }
    }
}
