package my.cardholder.ui.cloud.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.CloudProvider.Companion.getDrawableRes
import my.cardholder.databinding.ItemCloudProviderBinding
import my.cardholder.util.ext.setStartEndCompoundDrawables

class CloudProviderAdapter(
    private val onItemClicked: (cloudProvider: CloudProvider) -> Unit,
) : ListAdapter<CloudProviderState, CloudProviderAdapter.ItemViewHolder>(CloudProviderStateDiffCallback) {

    private companion object {
        object CloudProviderStateDiffCallback : DiffUtil.ItemCallback<CloudProviderState>() {
            override fun areItemsTheSame(oldItem: CloudProviderState, newItem: CloudProviderState) =
                oldItem.cloudProvider == newItem.cloudProvider

            override fun areContentsTheSame(oldItem: CloudProviderState, newItem: CloudProviderState) =
                oldItem.isSelected == newItem.isSelected && oldItem.isAvailable == newItem.isAvailable
        }
    }

    inner class ItemViewHolder(
        private val binding: ItemCloudProviderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val cloudProviderState = getItem(adapterPosition)
                onItemClicked.invoke(cloudProviderState.cloudProvider)
            }
        }

        fun bind(cloudProviderState: CloudProviderState) {
            binding.itemCloudProviderText.apply {
                val cloudProvider = cloudProviderState.cloudProvider
                text = cloudProvider.cloudName
                setStartEndCompoundDrawables(
                    startDrawableResId = cloudProvider.getDrawableRes(),
                    endDrawableResId = if (cloudProviderState.isSelected) R.drawable.ic_done else null
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemCloudProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
