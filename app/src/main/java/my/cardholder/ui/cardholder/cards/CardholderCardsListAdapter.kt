package my.cardholder.ui.cardholder.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.ItemCardBinding
import my.cardholder.util.loadBarcodeImage
import my.cardholder.util.setupUniqueTransitionName

class CardholderCardsListAdapter(
    private val onItemClick: (cardId: Long, sharedElements: Map<View, String>) -> Unit,
) : ListAdapter<Card, CardholderCardsListAdapter.CardViewHolder>(CardDiffCallback) {

    private companion object {
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
                val card = getItem(adapterPosition)
                val sharedElements = with(binding) {
                    mapOf(
                        itemCardBarcodeImage to itemCardBarcodeImage.transitionName,
                        itemCardNameText to itemCardNameText.transitionName,
                        itemCardTextText to itemCardTextText.transitionName,
                    )
                }
                onItemClick.invoke(card.id, sharedElements)
            }
        }

        fun bind(card: Card) {
            with(binding) {
                val uniqueNameSuffix = card.id
                itemCardLayout.setBackgroundColor(card.getColorInt())
                itemCardBarcodeImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    loadBarcodeImage(
                        barcodeFile = card.getBarcodeFile(context),
                        originalSize = false,
                    )
                }
                itemCardNameText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.name
                }
                itemCardTextText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.text
                }
            }
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
