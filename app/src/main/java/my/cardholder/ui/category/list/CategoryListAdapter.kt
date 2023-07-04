package my.cardholder.ui.category.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.CategoryAndCards
import my.cardholder.databinding.ItemCategoryBinding
import my.cardholder.util.ext.setupUniqueTransitionName

class CategoryListAdapter(
    private val onItemClicked: (categoryAndCards: CategoryAndCards, sharedElements: Map<View, String>) -> Unit,
) : ListAdapter<CategoryAndCards, CategoryListAdapter.CategoryViewHolder>(CategoryDiffCallback) {

    private companion object {
        object CategoryDiffCallback : DiffUtil.ItemCallback<CategoryAndCards>() {
            override fun areItemsTheSame(oldItem: CategoryAndCards, newItem: CategoryAndCards) =
                oldItem.category.id == newItem.category.id

            override fun areContentsTheSame(oldItem: CategoryAndCards, newItem: CategoryAndCards) =
                oldItem.category.name == newItem.category.name &&
                        oldItem.cards.size == newItem.cards.size
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
                val categoryAndCards = getItem(adapterPosition)
                onItemClicked.invoke(categoryAndCards, sharedElements)
            }
        }

        fun bind(categoryAndCards: CategoryAndCards) {
            with(binding) {
                itemCategoryNameText.setupUniqueTransitionName(categoryAndCards.category.id)
                itemCategoryNameText.text = categoryAndCards.category.name
                itemCategoryNumberOfCardsText.text = categoryAndCards.cards.size.toString()
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
