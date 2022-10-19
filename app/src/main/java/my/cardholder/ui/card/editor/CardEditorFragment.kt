package my.cardholder.ui.card.editor

import android.transition.TransitionInflater
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import coil.load
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.FragmentCardEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionNamesAndReturnSharedElements
import javax.inject.Inject

@AndroidEntryPoint
class CardEditorFragment : BaseFragment<FragmentCardEditorBinding>(
    FragmentCardEditorBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardEditorViewModelFactory

    private val args: CardEditorFragmentArgs by navArgs()

    override val viewModel: CardEditorViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        binding.setupUniqueTransitionNamesAndReturnSharedElements(
            uniqueSuffix = args.cardId,
            R.id.card_editor_card_name_input_layout,
            R.id.card_editor_card_text_input_layout,
            R.id.card_editor_barcode_image,
            R.id.card_editor_ok_fab,
        )
        binding.cardEditorOkFab.setOnClickListener {
            viewModel.onOkFabClicked()
        }
        binding.cardEditorColorPickerButton.setOnClickListener {
            showColorPickerDialog()
        }
        binding.cardEditorCardNameEditText.doAfterTextChanged {
            viewModel.onCardNameChanged(it?.toString())
        }
        binding.cardEditorCardTextEditText.doAfterTextChanged {
            viewModel.onCardTextChanged(it?.toString())
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            with(binding) {
                cardEditorBackgroundColorView.setBackgroundColor(card.getColorInt(requireContext()))
                cardEditorBarcodeImage.load(card.getBarcodeFile(requireContext()))
                cardEditorCardNameEditText.setText(card.name)
                cardEditorCardTextEditText.setText(card.text)
            }
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(R.string.card_color)
            .setPositiveButton(
                android.R.string.ok,
                ColorEnvelopeListener { envelope, _ ->
                    viewModel.onColorPickerResult(envelope.hexCode)
                }
            )
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(false)
            .show()
    }
}
