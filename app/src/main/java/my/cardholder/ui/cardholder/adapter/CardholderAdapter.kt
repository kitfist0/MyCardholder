package my.cardholder.ui.cardholder.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.ItemCardBinding
import my.cardholder.util.ext.loadBarcodeImage
import my.cardholder.util.ext.setupUniqueTransitionName

class CardholderAdapter(
    private val onItemClick: (cardId: Long, sharedElements: Map<View, String>) -> Unit,
) : ListAdapter<Card, CardholderAdapter.CardViewHolder>(CardDiffCallback) {

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
                        itemCardContentText to itemCardContentText.transitionName,
                    )
                }
                onItemClick.invoke(card.id, sharedElements)
            }
        }

        fun bind(card: Card) {
            with(binding) {
                val uniqueNameSuffix = card.id
                itemCardLayout.background = getCardGradientDrawable(card.getColorInt())
                itemCardBarcodeImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    loadBarcodeImage(
                        barcodeFileName = card.barcodeFileName,
                        originalSize = false,
                    )
                }
                itemCardNameText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.name
                }
                itemCardContentText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.content
                }
            }
        }

        private fun getCardGradientDrawable(colorInt: Int): GradientDrawable {
            val bottomLeftColor = ColorUtils.blendARGB(colorInt, Color.TRANSPARENT, 0.2f)
            return GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(bottomLeftColor, colorInt))
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
