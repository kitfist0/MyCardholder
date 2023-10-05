package my.cardholder.ui.card.search

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.databinding.ItemSearchCategoryBinding
import my.cardholder.databinding.ItemSearchCategoryHeaderBinding

sealed class CardSearchCategoryViewHolder<T : CardSearchCategoryItem>(
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(item: T)

    class DefaultViewHolder(
        private val binding: ItemSearchCategoryBinding,
        private val onItemClicked: (categoryName: String) -> Unit,
    ) : CardSearchCategoryViewHolder<CardSearchCategoryItem.DefaultItem>(binding.root) {

        init {
            binding.root.setOnClickListener {
                val name = binding.itemSearchCategoryChip.text.toString()
                onItemClicked.invoke(name)
            }
        }

        override fun bind(item: CardSearchCategoryItem.DefaultItem) {
            binding.itemSearchCategoryChip.text = item.name
        }
    }

    class HeaderViewHolder(
        private val binding: ItemSearchCategoryHeaderBinding,
        private val onItemClicked: () -> Unit,
    ) : CardSearchCategoryViewHolder<CardSearchCategoryItem.HeaderItem>(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClicked.invoke()
            }
        }

        override fun bind(item: CardSearchCategoryItem.HeaderItem) {
            val iconDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_edit)
            binding.itemSearchCategoryHeaderChip.chipIcon = iconDrawable
        }
    }
}
