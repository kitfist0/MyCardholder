package my.cardholder.ui.label.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Label
import my.cardholder.databinding.ItemLabelBinding

class LabelListAdapter(
    private val onItemClick: (label: Label) -> Unit,
) : ListAdapter<Label, LabelListAdapter.LabelViewHolder>(LabelDiffCallback) {

    private companion object {
        object LabelDiffCallback : DiffUtil.ItemCallback<Label>() {
            override fun areItemsTheSame(oldItem: Label, newItem: Label) =
                oldItem.text == newItem.text

            override fun areContentsTheSame(oldItem: Label, newItem: Label) = true
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

        fun bind(label: Label) {
            binding.itemLabelText.text = label.text
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
