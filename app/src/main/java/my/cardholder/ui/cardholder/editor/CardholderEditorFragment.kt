package my.cardholder.ui.cardholder.editor

import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.FragmentCardholderEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionName
import javax.inject.Inject

@AndroidEntryPoint
class CardholderEditorFragment : BaseFragment<FragmentCardholderEditorBinding>(
    FragmentCardholderEditorBinding::inflate
) {

    private companion object {
        const val COLOR_PICKER_ANIM_DURATION = 300L
    }

    @Inject
    lateinit var viewModelFactory: CardholderEditorViewModelFactory

    private val args: CardholderEditorFragmentArgs by navArgs()

    private val listAdapter by lazy {
        CardholderEditorColorsAdapter { color ->
            viewModel.onColorItemClicked(color)
        }
    }

    override val viewModel: CardholderEditorViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardEditorBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
            cardEditorCardNameInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardNameChanged(it?.toString()) }
            }
            cardEditorCardTextInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardTextChanged(it?.toString()) }
            }
            cardEditorOkFab.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener { viewModel.onOkFabClicked() }
            }
            cardEditorColorsRecyclerView.adapter = listAdapter
            (sharedElementEnterTransition as TransitionSet).doOnEnd {
                cardEditorColorsRecyclerView.animate()
                    .setDuration(COLOR_PICKER_ANIM_DURATION)
                    .alpha(1f)
                    .start()
            }
        }
    }

    override fun collectData() {
        viewModel.cardColors.collectWhenStarted { colors ->
            listAdapter.submitList(colors)
        }
        viewModel.card.collectWhenStarted { card ->
            with(binding) {
                cardEditorBackgroundColorView.setBackgroundColor(card.getColorInt())
                cardEditorBarcodeImage.load(card.getBarcodeFile(requireContext()))
                cardEditorCardNameInputLayout.editText?.setText(card.name)
                cardEditorCardTextInputLayout.editText?.setText(card.text)
            }
        }
    }
}
