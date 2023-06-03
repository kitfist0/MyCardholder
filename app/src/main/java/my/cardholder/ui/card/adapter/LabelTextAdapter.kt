package my.cardholder.ui.card.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemLabelTextBinding

class LabelTextAdapter(
    private val onItemClick: (labelText: String) -> Unit,
) : ListAdapter<String, LabelTextAdapter.LabelTextViewHolder>(LabelTextDiffCallback) {

    private companion object {
        object LabelTextDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = true
        }
    }

    inner class LabelTextViewHolder(
        private val binding: ItemLabelTextBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val labelText = getItem(adapterPosition)
                onItemClick.invoke(labelText)
            }
        }

        fun bind(labelText: String) {
            binding.itemLabelChip.text = labelText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelTextViewHolder {
        val binding = ItemLabelTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabelTextViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelTextViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
