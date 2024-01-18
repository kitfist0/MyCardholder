package my.cardholder.ui.card.content

import android.content.Context
import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.activity.OnBackPressedCallback
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardContentBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.showSoftInput
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import javax.inject.Inject

@AndroidEntryPoint
class CardContentFragment : BaseFragment<FragmentCardContentBinding>(
    FragmentCardContentBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardContentViewModelFactory

    private val args: CardContentFragmentArgs by navArgs()

    override val viewModel: CardContentViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardContentInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            cardContentInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardContentTextChanged(it?.toString()) }
            cardContentOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
        (sharedElementEnterTransition as TransitionSet).doOnEnd {
            binding.cardContentInputLayout.showSoftInput()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardContentState.Loading -> {
                    binding.cardContentInputLayout.isEnabled = false
                }
                is CardContentState.Success -> {
                    binding.cardContentInputLayout.isEnabled = true
                    binding.cardContentInputLayout.editText
                        ?.setTextAndSelectionIfRequired(state.cardContent)
                }
            }
        }
    }
}
