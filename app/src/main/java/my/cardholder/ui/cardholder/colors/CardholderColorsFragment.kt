package my.cardholder.ui.cardholder.colors

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderColorsBinding
import my.cardholder.util.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class CardholderColorsFragment : BottomSheetDialogFragment() {

    private val args: CardholderColorsFragmentArgs by navArgs()

    private var _binding: FragmentCardholderColorsBinding? = null

    val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: CardholderColorsViewModelFactory

    private val viewModel: CardholderColorsViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardholderColorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            pickerRecyclerView.adapter = CardholderColorsListAdapter(viewModel.colors) { color ->
                viewModel.onColorClicked(color)
            }
            pickerRecyclerView.setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
