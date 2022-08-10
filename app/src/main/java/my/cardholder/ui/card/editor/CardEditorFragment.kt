package my.cardholder.ui.card.editor

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardEditorBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardEditorFragment : BaseFragment<FragmentCardEditorBinding>(
    FragmentCardEditorBinding::inflate
) {

    override val viewModel: CardEditorViewModel by viewModels()

    override fun initViews() {
    }

    override fun collectData() {
    }
}
