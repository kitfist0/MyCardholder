package my.cardholder.ui.card.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card
import my.cardholder.databinding.ItemSearchBinding
import my.cardholder.util.ext.setupUniqueTransitionName

class CardSearchAdapter(
    private val onItemClicked: (cardId: Long, sharedElements: Map<View, String>) -> Unit,
) : ListAdapter<Card, CardSearchAdapter.SearchViewHolder>(CardDiffCallback) {

    private companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Card, newItem: Card) =
                oldItem == newItem
        }
    }

    inner class SearchViewHolder(
        private val binding: ItemSearchBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val card = getItem(adapterPosition)
                val sharedElements = with(binding) {
                    mapOf<View, String>(
                        itemSearchBarcodeImage to itemSearchBarcodeImage.transitionName,
                        itemSearchNameText to itemSearchNameText.transitionName,
                    )
                }
                onItemClicked.invoke(card.id, sharedElements)
            }
        }

        fun bind(card: Card) {
            with(binding) {
                val uniqueNameSuffix = card.id
                itemSearchBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
                itemSearchNameText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.name
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
