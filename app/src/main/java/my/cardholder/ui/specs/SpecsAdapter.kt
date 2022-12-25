package my.cardholder.ui.specs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
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
                with(binding.itemSpecExpandedViewsGroup) {
                    isVisible = !isVisible
                }
            }
        }

        fun bind(spec: FormatSpec) {
            with(binding) {
                itemSpecBarcodeNameText.text = spec.name
                itemSpecBarcodeImage.setBitmapFromAssets(spec.getFileName())
                itemSpecBarcodeCharactersText.apply {
                    text = context.getString(R.string.format_spec_valid_characters).format(spec.characters)
                }
                itemSpecBarcodeLengthText.apply {
                    text = context.getString(R.string.format_spec_length).format(spec.length)
                }
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
