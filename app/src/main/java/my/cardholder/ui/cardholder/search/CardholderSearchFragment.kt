package my.cardholder.ui.cardholder.search

import android.transition.TransitionInflater
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderSearchBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardholderSearchFragment : BaseFragment<FragmentCardholderSearchBinding>(
    FragmentCardholderSearchBinding::inflate
) {

    override val viewModel: CardholderSearchViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun collectData() {
    }
}
