package my.cardholder.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemSettingsMainBinding

class MainAdapter(
    private val onOptionClicked: (SettingId, String) -> Unit
) : ListAdapter<ListItem, MainAdapter.MainItemViewHolder>(MainDiffCallback()) {

    private val expandedItems = mutableSetOf<SettingId>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainItemViewHolder {
        val binding = ItemSettingsMainBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MainItemViewHolder(binding, ::onItemClick, onOptionClicked, ::isItemExpanded)
    }

    override fun onBindViewHolder(holder: MainItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun onItemClick(itemId: SettingId) {
        val position = currentList.indexOfFirst { it.id == itemId }
        if (position != -1) {
            val item = getItem(position)
            if (item.options.isNotEmpty()) {
                toggleExpansion(itemId, position)
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

    class MainItemViewHolder(
        private val binding: ItemSettingsMainBinding,
        private val onItemClicked: (SettingId) -> Unit,
        private val onOptionClicked: (SettingId, String) -> Unit,
        private val isItemExpanded: (SettingId) -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListItem) {
            binding.settingsItemTitle.text = item.id.getTitle()

            if (item.options.isNotEmpty()) {
                binding.settingsItemOptionsIcon.isVisible = true
                binding.root.setOnClickListener {
                    onItemClicked.invoke(item.id)
                }
                binding.root.isClickable = true
            } else {
                binding.settingsItemOptionsIcon.isVisible = false
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
            }

            setupOptionsRecyclerView(item.options, item.id)

            animateOptionsList(item.id)
        }

        private fun setupOptionsRecyclerView(options: List<ListItem.Option>, itemId: SettingId) {
            val optionsAdapter = OptionsAdapter { optionId ->
                onOptionClicked.invoke(itemId, optionId)
                collapseOptionsWithCallback(itemId)
            }

            binding.settingsItemOptionsRecyclerView.apply {
                adapter = optionsAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
            }

            optionsAdapter.submitList(options)
        }

        private fun animateOptionsList(itemId: SettingId) {
            val isExpanded = isItemExpanded(itemId)

            if (isExpanded) {
                binding.settingsItemOptionsRecyclerView.isVisible = true
                binding.settingsItemOptionsRecyclerView.alpha = 0f
                binding.settingsItemOptionsRecyclerView.translationY = -20f
                binding.settingsItemOptionsRecyclerView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            } else {
                binding.settingsItemOptionsRecyclerView.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction {
                        binding.settingsItemOptionsRecyclerView.isVisible = false
                    }
                    .start()
            }

            binding.settingsItemOptionsIcon.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(300)
                .start()
        }

        private fun collapseOptionsWithCallback(itemId: SettingId) {
            (bindingAdapter as? MainAdapter)?.collapseItem(itemId)
        }
    }
}

private class MainDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}
