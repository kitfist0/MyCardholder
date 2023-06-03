package my.cardholder.ui.card.display

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.View
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardDisplayBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.adapter.LabelTextAdapter
import my.cardholder.util.ext.*
import javax.inject.Inject

@AndroidEntryPoint
class CardDisplayFragment : BaseFragment<FragmentCardDisplayBinding>(
    FragmentCardDisplayBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardDisplayViewModelFactory

    private val args: CardDisplayFragmentArgs by navArgs()

    private val labelTextAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LabelTextAdapter(
            onItemClick = { labelText ->
            }
        )
    }

    override val viewModel: CardDisplayViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val uniqueNameSuffix = args.cardId
            cardDisplayBarcodeImage.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setPadding(getStatusBarHeight())
            }
            cardDisplayCardNameText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayCardContentText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayEditFab.setOnClickListener {
                val sharedElements = mapOf<View, String>(
                    cardDisplayCardNameText to cardDisplayCardNameText.transitionName,
                    cardDisplayCardContentText to cardDisplayCardContentText.transitionName,
                    cardDisplayEditFab to cardDisplayEditFab.transitionName,
                )
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onEditFabClicked(extras)
            }
            cardDisplayDeleteCardButton.setOnClickListener {
                viewModel.onDeleteCardButtonClicked()
            }
            cardDisplayCardLabelsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, true)
                adapter = labelTextAdapter
            }
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardDisplayDeleteCardButton.isVisible = false
            }
            transitionSet.doOnEnd {
                cardDisplayDeleteCardButton.animateVisibilityChange()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            with(binding) {
                when (state) {
                    is CardDisplayState.Loading ->
                        cardDisplayEditFab.isClickable = false
                    is CardDisplayState.Success -> {
                        cardDisplayBarcodeImage.apply {
                            setBackgroundColor(state.cardColor)
                            loadBarcodeImage(state.barcodeFile)
                        }
                        labelTextAdapter.submitList(state.cardLabels)
                        cardDisplayCardNameText.text = state.cardName
                        cardDisplayCardContentText.text = state.cardContent
                        cardDisplayEditFab.isClickable = true
                    }
                }
            }
        }
    }
}
