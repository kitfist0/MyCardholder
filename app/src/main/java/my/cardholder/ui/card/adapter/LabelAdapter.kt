package my.cardholder.ui.card.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemLabelBinding

class LabelAdapter(
    private val onItemClick: (labelText: String) -> Unit,
) : ListAdapter<String, LabelAdapter.LabelViewHolder>(LabelDiffCallback) {

    private companion object {
        object LabelDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = true
        }
    }

    inner class LabelViewHolder(
        private val binding: ItemLabelBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val label = getItem(adapterPosition)
                onItemClick.invoke(label)
            }
        }

        fun bind(labelText: String) {
            binding.itemLabelChip.text = labelText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val binding = ItemLabelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
