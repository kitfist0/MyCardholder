package my.cardholder.ui.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import my.cardholder.R
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.ItemCardBinding
import my.cardholder.util.setupUniqueTransitionNamesAndReturnSharedElements

class CardsListAdapter(
    private val onItemClick: (cardId: Long, sharedElements: Map<View, String>) -> Unit,
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
                val card = getItem(adapterPosition)
                val sharedElements = binding.setupUniqueTransitionNamesAndReturnSharedElements(
                    uniqueSuffix = card.id,
                    R.id.item_card_name_text,
                    R.id.item_card_text_text,
                    R.id.item_card_barcode_image,
                )
                onItemClick.invoke(card.id, sharedElements)
            }
        }

        fun bind(card: Card) {
            with(binding) {
                itemCardLayout.setBackgroundColor(card.getColorInt(root.context))
                itemCardBarcodeImage.load(card.getBarcodeFile(root.context))
                itemCardNameText.text = card.name
                itemCardTextText.text = card.text
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
