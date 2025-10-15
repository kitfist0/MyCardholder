package my.cardholder.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemSettingsOptionBinding

class OptionsAdapter(
    private val onOptionClick: (String) -> Unit
) : ListAdapter<ListItem.Option, OptionsAdapter.OptionViewHolder>(OptionsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemSettingsOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding, onOptionClick)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OptionViewHolder(
        private val binding: ItemSettingsOptionBinding,
        private val onOptionClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: ListItem.Option) {
            binding.settingsItemOptionTitle.text = option.title
            binding.root.setOnClickListener {
                onOptionClick(option.id)
            }
        }
    }

    private class OptionsDiffCallback : DiffUtil.ItemCallback<ListItem.Option>() {
        override fun areItemsTheSame(oldItem: ListItem.Option, newItem: ListItem.Option): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListItem.Option, newItem: ListItem.Option): Boolean {
            return oldItem.selected == newItem.selected
        }
    }
}
