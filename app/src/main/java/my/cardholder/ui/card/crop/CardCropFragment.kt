package my.cardholder.ui.card.crop

import android.net.Uri
import androidx.fragment.app.viewModels
import com.canhub.cropper.CropImageOptions
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardCropBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardCropFragment : BaseFragment<FragmentCardCropBinding>(
    FragmentCardCropBinding::inflate
) {

    override val viewModel: CardCropViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            cardCropImageView.setImageCropOptions(CropImageOptions())
            cardCropImageView.setOnCropImageCompleteListener { _, result ->
                val inputImage = result.bitmap
                    ?.let { InputImage.fromBitmap(it, 0) }
                viewModel.onCropCompleted(inputImage)
            }
            cardCropOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.cardCropImageView.apply {
                imageUri ?: setImageUriAsync(Uri.parse(state.selectedImageUri))
            }
            if (state.cropButtonClickEvent) {
                binding.cardCropImageView.croppedImageAsync()
                viewModel.consumeCropButtonClickEvent()
            }
        }
    }
}
