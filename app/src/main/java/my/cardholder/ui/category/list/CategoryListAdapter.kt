package my.cardholder.ui.category.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemCategoryBinding
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.toNavExtras

class CategoryListAdapter(
    private val onItemClicked: (item: CategoryListItem, navExtras: FragmentNavigator.Extras) -> Unit,
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
                val categoryListItem = getItem(adapterPosition)
                val extras = listOf(binding.itemCategoryNameText).toNavExtras()
                onItemClicked.invoke(categoryListItem, extras)
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
