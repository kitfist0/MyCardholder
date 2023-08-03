package my.cardholder.ui.card.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemSearchCategoryBinding

class CardSearchCategoryAdapter(
    private val onItemClicked: (categoryName: String) -> Unit,
) : ListAdapter<String, CardSearchCategoryAdapter.SearchCategoryViewHolder>(CategoryNameDiffCallback) {

    private companion object {
        object CategoryNameDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) =
                oldItem.hashCode() == newItem.hashCode()

            override fun areContentsTheSame(oldItem: String, newItem: String) =
                oldItem == newItem
        }
    }

    inner class SearchCategoryViewHolder(
        private val binding: ItemSearchCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val categoryName = getItem(adapterPosition)
                onItemClicked.invoke(categoryName)
            }
        }

        fun bind(categoryName: String) {
            binding.itemSearchCategoryChip.text = categoryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCategoryViewHolder {
        return SearchCategoryViewHolder(
            ItemSearchCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
