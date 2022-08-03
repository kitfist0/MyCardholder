package my.cardholder.ui.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.Card
import my.cardholder.databinding.ItemCardBinding

class CardsListAdapter(
    private val onItemClick: (cardId: Long, sharedElementsMap: Map<View, String>) -> Unit,
) : ListAdapter<Card, CardsListAdapter.CardViewHolder>(CardDiffCallback) {

    companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Card, newItem: Card) =
                oldItem == newItem
        }
    }

    inner class CardViewHolder(
        private val binding: ItemCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val cardId = getItem(adapterPosition).id
                val sharedElements = mapOf(
                    binding.itemCardTitleText to "title_$cardId",
                    binding.itemCardSubtitleText to "subtitle_$cardId",
                    binding.itemCardBarcodeImage to "barcode_$cardId",
                ).onEach { ViewCompat.setTransitionName(it.key, it.value) }
                onItemClick.invoke(cardId, sharedElements)
            }
        }

        fun bind(card: Card) {
            binding.itemCardTitleText.text = card.title
            binding.itemCardSubtitleText.text = card.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
