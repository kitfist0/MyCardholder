package my.cardholder.ui.category.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.CategoryAndCards
import my.cardholder.databinding.ItemCategoryBinding

class CategoryListAdapter(
    private val onItemClicked: (categoryAndCards: CategoryAndCards) -> Unit,
) : ListAdapter<CategoryAndCards, CategoryListAdapter.CategoryViewHolder>(LabelDiffCallback) {

    private companion object {
        object LabelDiffCallback : DiffUtil.ItemCallback<CategoryAndCards>() {
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
                val label = getItem(adapterPosition)
                onItemClicked.invoke(label)
            }
        }

        fun bind(categoryAndCards: CategoryAndCards) {
            with(binding) {
                itemCategoryNameText.text = categoryAndCards.category.name
                itemCategoryNumberOfCardsText.text = categoryAndCards.cards.size.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
