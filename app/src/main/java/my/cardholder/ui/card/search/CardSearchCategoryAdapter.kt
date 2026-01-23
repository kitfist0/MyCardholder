package my.cardholder.ui.card.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import my.cardholder.databinding.ItemSearchCategoryBinding
import my.cardholder.databinding.ItemSearchCategoryHeaderBinding

class CardSearchCategoryAdapter(
    private val onCategoryClicked: (categoryName: String) -> Unit,
    private val onHeaderClicked: () -> Unit,
) : ListAdapter<CardSearchCategoryItem, CardSearchCategoryViewHolder<CardSearchCategoryItem>>(
    CategoryDiffCallback
) {

    private companion object {
        const val VIEW_TYPE_CATEGORY_DEFAULT = 0
        const val VIEW_TYPE_CATEGORY_HEADER = 1

        object CategoryDiffCallback : DiffUtil.ItemCallback<CardSearchCategoryItem>() {
            override fun areItemsTheSame(
                oldItem: CardSearchCategoryItem,
                newItem: CardSearchCategoryItem
            ) = when {
                oldItem is CardSearchCategoryItem.DefaultItem && newItem is CardSearchCategoryItem.DefaultItem ->
                    oldItem.name == newItem.name
                else -> true
            }

            override fun areContentsTheSame(
                oldItem: CardSearchCategoryItem,
                newItem: CardSearchCategoryItem
            ) = when {
                oldItem is CardSearchCategoryItem.DefaultItem && newItem is CardSearchCategoryItem.DefaultItem ->
                    oldItem == newItem
                else -> true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CardSearchCategoryItem.DefaultItem -> VIEW_TYPE_CATEGORY_DEFAULT
            is CardSearchCategoryItem.HeaderItem -> VIEW_TYPE_CATEGORY_HEADER
            else -> throw RuntimeException("Unsupported ItemViewType for obj ${getItem(position)}")
        }
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardSearchCategoryViewHolder<CardSearchCategoryItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY_DEFAULT -> CardSearchCategoryViewHolder.DefaultViewHolder(
                ItemSearchCategoryBinding.inflate(inflater, parent, false),
                onCategoryClicked,
            )

            VIEW_TYPE_CATEGORY_HEADER -> CardSearchCategoryViewHolder.HeaderViewHolder(
                ItemSearchCategoryHeaderBinding.inflate(inflater, parent, false),
                onHeaderClicked,
            )

            else -> throw RuntimeException("Unsupported view holder type")
        } as CardSearchCategoryViewHolder<CardSearchCategoryItem>
    }

    override fun onBindViewHolder(
        holder: CardSearchCategoryViewHolder<CardSearchCategoryItem>,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}
