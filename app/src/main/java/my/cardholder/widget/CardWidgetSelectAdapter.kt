package my.cardholder.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.CardAndCategory
import my.cardholder.databinding.ItemCardWidgetSelectBinding

class CardWidgetSelectAdapter(
    private val onCardClicked: (cardId: Long) -> Unit,
) : ListAdapter<CardAndCategory, CardWidgetSelectAdapter.CardViewHolder>(CardDiffCallback) {

    private companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<CardAndCategory>() {
            override fun areItemsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem.card.id == newItem.card.id

            override fun areContentsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardWidgetSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(
        private val binding: ItemCardWidgetSelectBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cardAndCategory: CardAndCategory) {
            val card = cardAndCategory.card
            binding.itemCardWidgetSelectColorCard.setCardBackgroundColor(card.getColorInt())
            binding.itemCardWidgetSelectNameText.text = card.name
            binding.root.setOnClickListener { onCardClicked(card.id) }
        }
    }
}
