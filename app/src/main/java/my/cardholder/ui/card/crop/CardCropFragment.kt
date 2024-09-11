package my.cardholder.ui.card.crop

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardCropBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardCropFragment : BaseFragment<FragmentCardCropBinding>(
    FragmentCardCropBinding::inflate
) {

    override val viewModel: CardCropViewModel by viewModels()

    override fun initViews() {
    }

    override fun collectData() {
    }
}
