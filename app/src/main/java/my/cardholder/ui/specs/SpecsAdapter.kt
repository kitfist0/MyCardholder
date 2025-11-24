package my.cardholder.ui.specs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import my.cardholder.R
import my.cardholder.data.model.FormatSpec
import my.cardholder.data.model.FormatSpec.Companion.getFileName
import my.cardholder.databinding.ItemSpecBinding
import my.cardholder.util.ext.setBitmapFromAssets

class SpecsAdapter(
    private val items: List<FormatSpec>,
) : RecyclerView.Adapter<SpecsAdapter.SpecViewHolder>() {

    inner class SpecViewHolder(
        private val binding: ItemSpecBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                with(binding) {
                    val itemIsExpanded = itemSpecBarcodeCharactersText.isVisible && itemSpecBarcodeLengthText.isVisible
                    val cardColor = MaterialColors.getColor(
                        root,
                        if (itemIsExpanded) {
                            android.R.attr.windowBackground
                        } else {
                            com.google.android.material.R.attr.colorSurfaceContainer
                        }
                    )
                    itemSpecCard.setCardBackgroundColor(cardColor)
                    itemSpecBarcodeCharactersText.animateVisibility(itemIsExpanded)
                    itemSpecBarcodeLengthText.animateVisibility(itemIsExpanded)
                }
            }
        }

        fun bind(spec: FormatSpec) {
            with(binding) {
                itemSpecBarcodeNameText.text = spec.name
                itemSpecBarcodeImage.setBitmapFromAssets(spec.getFileName())
                itemSpecBarcodeCharactersText.apply {
                    text = context.getString(R.string.specs_barcode_valid_characters_text).format(spec.characters)
                }
                itemSpecBarcodeLengthText.apply {
                    text = context.getString(R.string.specs_barcode_length_text).format(spec.length)
                }
            }
        }

        private fun View.animateVisibility(isVisibleNow: Boolean) {
            if (isVisibleNow) {
                animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction {
                        isVisible = false
                    }
                    .start()
            } else {
                isVisible = true
                alpha = 0f
                translationY = -20f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecViewHolder {
        val binding = ItemSpecBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpecViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpecViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
