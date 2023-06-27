package my.cardholder.ui.category.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Category
import my.cardholder.databinding.ItemCategoryBinding

class CategoryListAdapter(
    private val onItemClicked: (category: Category) -> Unit,
) : ListAdapter<Category, CategoryListAdapter.CategoryViewHolder>(LabelDiffCallback) {

    private companion object {
        object LabelDiffCallback : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Category, newItem: Category) =
                oldItem.name == newItem.name
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

        fun bind(category: Category) {
            binding.itemCategoryNameText.text = category.name
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
