package my.cardholder.ui.specs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.data.model.FormatCharacters
import my.cardholder.data.model.FormatLength
import my.cardholder.data.model.FormatSpec
import my.cardholder.data.model.FormatSpec.Companion.getFileName
import my.cardholder.databinding.ItemSpecBinding
import my.cardholder.databinding.ItemSpecHeaderBinding
import my.cardholder.util.ext.setBitmapFromAssets

class SpecsAdapter : ListAdapter<SpecsItem, RecyclerView.ViewHolder>(SpecsDiffCallback()) {

    class HeaderViewHolder(
        private val binding: ItemSpecHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: SpecsItem.Header) {
            binding.root.setText(header.titleRes)
        }
    }

    class FormatViewHolder(
        private val binding: ItemSpecBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(spec: FormatSpec) {
            with(binding) {
                val context = root.context
                itemSpecBarcodeNameText.text = spec.name
                itemSpecBarcodeImage.setBitmapFromAssets(spec.getFileName())
                itemSpecBarcodeCharactersText.text = context.getString(
                    R.string.specs_barcode_valid_characters_text,
                    context.getCharactersText(spec.characters),
                )
                itemSpecBarcodeLengthText.text = context.getString(
                    R.string.specs_barcode_length_text,
                    context.getLengthText(spec.length),
                )
            }
        }

        private fun Context.getCharactersText(characters: FormatCharacters): String {
            return getString(
                when (characters) {
                    FormatCharacters.DIGITS -> R.string.specs_characters_digits
                    FormatCharacters.CODABAR -> R.string.specs_characters_codabar
                    FormatCharacters.ALPHANUMERIC -> R.string.specs_characters_alphanumeric
                    FormatCharacters.ASCII_128 -> R.string.specs_characters_ascii_128
                    FormatCharacters.ASCII_255 -> R.string.specs_characters_ascii_255
                }
            )
        }

        private fun Context.getLengthText(length: FormatLength): String {
            return when (length) {
                is FormatLength.Fixed -> length.value.toString()
                FormatLength.Even -> getString(R.string.specs_length_even)
                FormatLength.Unfixed -> getString(R.string.specs_length_unfixed)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SpecsItem.Header -> VIEW_TYPE_HEADER
            is SpecsItem.Format -> VIEW_TYPE_FORMAT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemSpecHeaderBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> FormatViewHolder(ItemSpecBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SpecsItem.Header -> (holder as HeaderViewHolder).bind(item)
            is SpecsItem.Format -> (holder as FormatViewHolder).bind(item.spec)
        }
    }

    private companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_FORMAT = 1
    }
}

private class SpecsDiffCallback : DiffUtil.ItemCallback<SpecsItem>() {
    override fun areItemsTheSame(oldItem: SpecsItem, newItem: SpecsItem): Boolean {
        return when (oldItem) {
            is SpecsItem.Header if newItem is SpecsItem.Header ->
                oldItem.titleRes == newItem.titleRes

            is SpecsItem.Format if newItem is SpecsItem.Format ->
                oldItem.spec.format == newItem.spec.format

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: SpecsItem, newItem: SpecsItem): Boolean {
        return oldItem == newItem
    }
}
