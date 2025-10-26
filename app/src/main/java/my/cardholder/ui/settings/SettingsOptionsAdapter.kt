package my.cardholder.ui.settings

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemSettingsOptionBinding

class SettingsOptionsAdapter(
    private val onOptionClick: (String) -> Unit
) : ListAdapter<ListItem.Option, SettingsOptionsAdapter.ItemViewHolder>(OptionsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemSettingsOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, onOptionClick)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemViewHolder(
        private val binding: ItemSettingsOptionBinding,
        private val onOptionClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: ListItem.Option) {
            with(binding) {
                root.setOnClickListener {
                    onOptionClick(option.id)
                }
                settingsItemOptionTitle.text = option.title
                val textStyle = if (option.selected) Typeface.BOLD else Typeface.NORMAL
                settingsItemOptionTitle.setTypeface(null, textStyle)
            }
        }
    }

    private class OptionsDiffCallback : DiffUtil.ItemCallback<ListItem.Option>() {
        override fun areItemsTheSame(oldItem: ListItem.Option, newItem: ListItem.Option): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ListItem.Option,
            newItem: ListItem.Option
        ): Boolean {
            return oldItem.selected == newItem.selected
        }
    }
}
