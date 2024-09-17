package my.cardholder.ui.card.crop

import android.net.Uri
import androidx.core.view.isVisible
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
                viewModel.onProcessingCompleted(inputImage)
            }
            cardCropOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardCropState.Selection -> {
                    binding.cardCropProgress.isVisible = false
                    binding.cardCropOkFab.isVisible = true
                    binding.cardCropImageView.apply {
                        isVisible = true
                        imageUri ?: setImageUriAsync(Uri.parse(state.selectedImageUri))
                    }
                    if (state.startProcessingEvent) {
                        binding.cardCropImageView.croppedImageAsync()
                        viewModel.onProcessingStarted()
                    }
                }
                is CardCropState.Processing -> {
                    binding.cardCropProgress.isVisible = true
                    binding.cardCropImageView.isVisible = false
                    binding.cardCropOkFab.isVisible = false
                }
            }
        }
    }
}
