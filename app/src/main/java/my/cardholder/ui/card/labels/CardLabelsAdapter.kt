package my.cardholder.ui.card.labels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.databinding.ItemCardLabelBinding
import my.cardholder.util.ext.setDrawables

class CardLabelsAdapter(
    private val onItemClick: (itemState: CardLabelsItemState) -> Unit,
) : ListAdapter<CardLabelsItemState, CardLabelsAdapter.CardLabelViewHolder>(CardLabelDiffCallback) {

    private companion object {
        object CardLabelDiffCallback : DiffUtil.ItemCallback<CardLabelsItemState>() {
            override fun areItemsTheSame(oldItem: CardLabelsItemState, newItem: CardLabelsItemState) =
                oldItem.labelValue == newItem.labelValue

            override fun areContentsTheSame(oldItem: CardLabelsItemState, newItem: CardLabelsItemState) =
                oldItem.isChecked == newItem.isChecked
        }
    }

    inner class CardLabelViewHolder(
        private val binding: ItemCardLabelBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val cardLabel = getItem(adapterPosition)
                onItemClick.invoke(cardLabel)
            }
        }

        fun bind(cardLabel: CardLabelsItemState) {
            binding.itemCardLabelText.apply {
                text = cardLabel.labelValue
                val drawableRes = if (cardLabel.isChecked) {
                    R.drawable.ic_button_checked
                } else {
                    R.drawable.ic_button_unchecked
                }
                setDrawables(end = drawableRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardLabelViewHolder {
        val binding = ItemCardLabelBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CardLabelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardLabelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
