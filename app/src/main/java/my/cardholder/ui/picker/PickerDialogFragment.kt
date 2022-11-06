package my.cardholder.ui.picker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentColorPickerBinding
import my.cardholder.util.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class PickerDialogFragment : BottomSheetDialogFragment() {

    private val args: PickerDialogFragmentArgs by navArgs()

    private var _binding: FragmentColorPickerBinding? = null

    val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: PickerViewModelFactory

    private val viewModel: PickerViewModel by assistedViewModels {
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
        _binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            pickerRecyclerView.adapter = PickerListAdapter(viewModel.colors) { color ->
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
