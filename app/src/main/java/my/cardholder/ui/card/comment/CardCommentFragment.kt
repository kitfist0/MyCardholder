package my.cardholder.ui.card.comment

import android.content.Context
import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.activity.OnBackPressedCallback
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardCommentBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.content.CardContentState
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.showSoftInput
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardCommentFragment : BaseFragment<FragmentCardCommentBinding>(
    FragmentCardCommentBinding::inflate
) {

    private val args: CardCommentFragmentArgs by navArgs()

    override val viewModel: CardCommentViewModel by viewModels()

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
            cardCommentInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            cardCommentInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardCommentTextChanged(it?.toString()) }
            cardCommentOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
        (sharedElementEnterTransition as TransitionSet).doOnEnd {
            binding.cardCommentInputLayout.showSoftInput()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardContentState.Loading ->
                    binding.cardCommentInputLayout.isEnabled = false

                is CardContentState.Success ->
                    binding.cardCommentInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardContent)
                    }
            }
        }
    }
}
