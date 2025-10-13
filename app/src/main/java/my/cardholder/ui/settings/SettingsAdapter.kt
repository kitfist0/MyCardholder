package my.cardholder.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.databinding.ItemSettingsDefaultBinding
import my.cardholder.databinding.ItemSettingsHeaderBinding
import my.cardholder.util.ext.setStartEndCompoundDrawables

class SettingsAdapter(
    private val onItemClicked: (SettingsListItem) -> Unit,
) : ListAdapter<SettingsListItem, RecyclerView.ViewHolder>(SettingsListItemDiffCallback) {

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1

        object SettingsListItemDiffCallback : DiffUtil.ItemCallback<SettingsListItem>() {
            override fun areItemsTheSame(
                oldItem: SettingsListItem,
                newItem: SettingsListItem
            ): Boolean {
                return when {
                    oldItem is SettingsListItem.Header && newItem is SettingsListItem.Header ->
                        oldItem.cloudSyncEnabled == newItem.cloudSyncEnabled

                    oldItem is SettingsListItem.Item && newItem is SettingsListItem.Item ->
                        oldItem.id == newItem.id

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: SettingsListItem,
                newItem: SettingsListItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemSettingsHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClicked.invoke(getItem(adapterPosition))
            }
        }

        fun bind(header: SettingsListItem.Header) {
            binding.itemSettingsHeaderText.apply {
                setStartEndCompoundDrawables(
                    endDrawableResId = if (header.cloudSyncEnabled) {
                        R.drawable.ic_cloud_on
                    } else {
                        R.drawable.ic_cloud_off
                    }
                )
                setText(
                    if (header.cloudSyncEnabled) {
                        R.string.settings_cloud_sync_switch_on_text
                    } else {
                        R.string.settings_cloud_sync_switch_off_text
                    }
                )
            }
        }
    }

    inner class ItemViewHolder(
        private val binding: ItemSettingsDefaultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClicked.invoke(getItem(adapterPosition))
            }
        }

        fun bind(item: SettingsListItem.Item) {
            with(binding) {
                itemSettingsDefaultImage.setImageResource(item.id.getImageRes())
                itemSettingsDefaultTitleText.text = item.id.getTitle()
                itemSettingsDefaultSubtitleText.apply {
                    text = item.subtitle
                    isGone = item.subtitle.isNullOrEmpty()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsListItem.Header -> TYPE_HEADER
            is SettingsListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER ->
                HeaderViewHolder(
                    ItemSettingsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            TYPE_ITEM ->
                ItemViewHolder(
                    ItemSettingsDefaultBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            else ->
                throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as SettingsListItem.Header)
            is ItemViewHolder -> holder.bind(getItem(position) as SettingsListItem.Item)
        }
    }
}
