package my.cardholder.ui.category.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemCategoryBinding
import my.cardholder.util.ext.setupUniqueTransitionName

class CategoryListAdapter(
    private val onItemClicked: (item: CategoryListItem, sharedElements: Map<View, String>) -> Unit,
) : ListAdapter<CategoryListItem, CategoryListAdapter.CategoryViewHolder>(CategoryListItemDiffCallback) {

    private companion object {
        object CategoryListItemDiffCallback : DiffUtil.ItemCallback<CategoryListItem>() {
            override fun areItemsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem) =
                oldItem.categoryId == newItem.categoryId

            override fun areContentsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem) =
                oldItem.categoryName == newItem.categoryName &&
                        oldItem.numOfCards == newItem.numOfCards
        }
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val sharedElements = with(binding) {
                    mapOf<View, String>(
                        itemCategoryNameText to itemCategoryNameText.transitionName,
                    )
                }
                val categoryListItem = getItem(adapterPosition)
                onItemClicked.invoke(categoryListItem, sharedElements)
            }
        }

        fun bind(categoryListItem: CategoryListItem) {
            with(binding) {
                itemCategoryNameText.setupUniqueTransitionName(categoryListItem.categoryId)
                itemCategoryNameText.text = categoryListItem.categoryName
                itemCategoryNumberOfCardsText.text = categoryListItem.numOfCards.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
