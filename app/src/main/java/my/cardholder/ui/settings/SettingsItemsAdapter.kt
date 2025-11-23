package my.cardholder.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import my.cardholder.databinding.ItemSettingsMainBinding

class SettingsItemsAdapter(
    private val onItemWithoutOptionsClicked: (SettingId) -> Unit,
    private val onItemOptionClicked: (SettingId, String) -> Unit,
) : ListAdapter<SettingsItem, SettingsItemsAdapter.ItemViewHolder>(MainDiffCallback()) {

    private val expandedItems = mutableSetOf<SettingId>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemSettingsMainBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, ::onItemClick, onItemOptionClicked, ::isItemExpanded)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun onItemClick(itemId: SettingId) {
        val position = currentList.indexOfFirst { it.id == itemId }
        if (position != -1) {
            val item = getItem(position)
            if (item.options.isNotEmpty()) {
                toggleExpansion(itemId, position)
            } else {
                collapseAll()
                onItemWithoutOptionsClicked.invoke(itemId)
            }
        }
    }

    private fun isItemExpanded(itemId: SettingId): Boolean {
        return expandedItems.contains(itemId)
    }

    private fun toggleExpansion(itemId: SettingId, position: Int) {
        val wasExpanded = expandedItems.contains(itemId)

        if (wasExpanded) {
            expandedItems.remove(itemId)
            notifyItemChanged(position)
        } else {
            val previousExpanded = expandedItems.firstOrNull()

            expandedItems.clear()
            expandedItems.add(itemId)

            if (previousExpanded != null) {
                val previousPosition = currentList.indexOfFirst { it.id == previousExpanded }
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
            }
            notifyItemChanged(position)
        }
    }

    fun collapseItem(itemId: SettingId) {
        if (expandedItems.contains(itemId)) {
            expandedItems.remove(itemId)
            val position = currentList.indexOfFirst { it.id == itemId }
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }

    fun collapseAll() {
        if (expandedItems.isNotEmpty()) {
            val itemsToCollapse = expandedItems.toList()
            expandedItems.clear()

            itemsToCollapse.forEach { itemId ->
                val position = currentList.indexOfFirst { it.id == itemId }
                if (position != -1) {
                    notifyItemChanged(position)
                }
            }
        }
    }

    class ItemViewHolder(
        private val binding: ItemSettingsMainBinding,
        private val onItemClicked: (SettingId) -> Unit,
        private val onOptionClicked: (SettingId, String) -> Unit,
        private val isItemExpanded: (SettingId) -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem) {
            with(binding) {
                settingsItemIcon.setImageResource(item.iconRes)
                settingsItemTitle.setText(item.id.getTitle())
                settingsItemOptionsIcon.isVisible = item.options.isNotEmpty()
                root.setOnClickListener {
                    onItemClicked.invoke(item.id)
                }
            }

            setupOptionsRecyclerView(item.options, item.id)

            animateOptionsList(item.id)
        }

        private fun setupOptionsRecyclerView(options: List<SettingsItem.Option>, itemId: SettingId) {
            val settingsOptionsAdapter = SettingsOptionsAdapter { optionId ->
                onOptionClicked.invoke(itemId, optionId)
                collapseOptionsWithCallback(itemId)
            }

            binding.settingsItemOptionsRecyclerView.apply {
                adapter = settingsOptionsAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
            }

            settingsOptionsAdapter.submitList(options)
        }

        private fun animateOptionsList(itemId: SettingId) {
            val isExpanded = isItemExpanded(itemId)

            if (isExpanded) {
                val cardColor = MaterialColors.getColor(binding.root, R.attr.colorSurfaceContainer)
                binding.settingsItemCard.setCardBackgroundColor(cardColor)
                binding.settingsItemOptionsRecyclerView.apply {
                    isVisible = true
                    alpha = 0f
                    translationY = -20f
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
            } else {
                val cardColor = MaterialColors.getColor(binding.root, android.R.attr.windowBackground)
                binding.settingsItemCard.setCardBackgroundColor(cardColor)
                binding.settingsItemOptionsRecyclerView.apply {
                    animate()
                        .alpha(0f)
                        .translationY(-20f)
                        .setDuration(200)
                        .withEndAction {
                            isVisible = false
                        }
                        .start()
                }
            }

            binding.settingsItemOptionsIcon.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(300)
                .start()
        }

        private fun collapseOptionsWithCallback(itemId: SettingId) {
            (bindingAdapter as? SettingsItemsAdapter)?.collapseItem(itemId)
        }
    }
}

private class MainDiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
    override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem == newItem
    }
}
