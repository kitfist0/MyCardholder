package my.cardholder.shortcut

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.CardAndCategory
import my.cardholder.databinding.ItemCardShortcutSelectBinding

class CardShortcutSelectAdapter(
    private val onCardClicked: (cardId: Long) -> Unit,
) : ListAdapter<CardAndCategory, CardShortcutSelectAdapter.CardViewHolder>(CardDiffCallback) {

    private companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<CardAndCategory>() {
            override fun areItemsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem.card.id == newItem.card.id

            override fun areContentsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardShortcutSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(
        private val binding: ItemCardShortcutSelectBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cardAndCategory: CardAndCategory) {
            val card = cardAndCategory.card
            binding.itemCardShortcutSelectColorCard.setCardBackgroundColor(card.getColorInt())
            binding.itemCardShortcutSelectNameText.text = card.name
            binding.root.setOnClickListener { onCardClicked(card.id) }
        }
    }
}
