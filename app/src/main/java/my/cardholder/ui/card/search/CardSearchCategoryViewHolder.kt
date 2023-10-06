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
            binding.itemSearchCategoryHeaderChip.apply {
                when (item) {
                    CardSearchCategoryItem.HeaderItem.AddCategories -> {
                        chipIcon = null
                        setText(R.string.card_search_add_categories_header_text)
                    }
                    CardSearchCategoryItem.HeaderItem.EditCategories -> {
                        chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit)
                        text = null
                    }
                }
            }
        }
    }
}
