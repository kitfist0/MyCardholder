package my.cardholder.ui.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.Card
import my.cardholder.R

class CardsListAdapter(
    private val onItemClick: (cardId: Long) -> Unit,
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
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { onItemClick.invoke(getItem(adapterPosition).id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.item)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        getItem(position).apply {
            holder.itemView.findViewById<TextView>(R.id.item_card_title_text).text = title
            holder.itemView.findViewById<TextView>(R.id.item_card_subtitle_text).text = text
        }
    }
}
