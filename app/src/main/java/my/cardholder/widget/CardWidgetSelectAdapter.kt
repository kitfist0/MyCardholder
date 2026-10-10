package my.cardholder.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.ItemCardWidgetSelectBinding

class CardWidgetSelectAdapter(
    private val onCardClicked: (cardId: Long) -> Unit,
) : ListAdapter<CardWidgetConfigureState.Item, CardWidgetSelectAdapter.CardViewHolder>(ItemDiffCallback) {

    private companion object {
        object ItemDiffCallback : DiffUtil.ItemCallback<CardWidgetConfigureState.Item>() {
            override fun areItemsTheSame(
                oldItem: CardWidgetConfigureState.Item,
                newItem: CardWidgetConfigureState.Item,
            ) = oldItem.card.id == newItem.card.id

            override fun areContentsTheSame(
                oldItem: CardWidgetConfigureState.Item,
                newItem: CardWidgetConfigureState.Item,
            ) = oldItem == newItem
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

        fun bind(item: CardWidgetConfigureState.Item) {
            val card = item.card
            binding.itemCardWidgetSelectColorCard.setCardBackgroundColor(card.getColorInt())
            binding.itemCardWidgetSelectNameText.text = card.name
            binding.itemCardWidgetSelectCheckBox.isChecked = item.isSelected
            binding.root.setOnClickListener { onCardClicked(card.id) }
        }
    }
}
