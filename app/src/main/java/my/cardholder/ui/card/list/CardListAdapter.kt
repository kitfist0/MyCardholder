package my.cardholder.ui.card.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.CardAndCategory
import my.cardholder.databinding.ItemCardBinding
import my.cardholder.util.ext.loadBarcodeImage
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.toNavExtras

class CardListAdapter(
    private val onItemClicked: (cardId: Long, navExtras: FragmentNavigator.Extras) -> Unit,
    private val onItemLongClicked: (cardId: Long) -> Unit,
    private val onItemCountIncreased: () -> Unit,
) : ListAdapter<CardAndCategory, CardListAdapter.CardViewHolder>(CardDiffCallback) {

    private companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<CardAndCategory>() {
            override fun areItemsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem.card.id == newItem.card.id

            override fun areContentsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem == newItem
        }
    }

    inner class CardViewHolder(
        private val binding: ItemCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val cardAndCategory = getItem(adapterPosition)
                val extras = listOf(
                    binding.itemCardBarcodeImage,
                    binding.itemCardNameText,
                    binding.itemCardContentText,
                    binding.itemCardCategoryText,
                ).toNavExtras()
                onItemClicked.invoke(cardAndCategory.card.id, extras)
            }
            itemView.setOnLongClickListener {
                val cardAndCategory = getItem(adapterPosition)
                onItemLongClicked.invoke(cardAndCategory.card.id)
                true
            }
        }

        fun bind(cardAndCategory: CardAndCategory) {
            with(binding) {
                val card = cardAndCategory.card
                val uniqueNameSuffix = card.id
                itemCardLayout.background = getCardGradientDrawable(card.getColorInt())
                itemCardBarcodeImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    loadBarcodeImage(
                        barcodeFile = card.barcodeFile,
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
                itemCardCategoryText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = cardAndCategory.category?.name.orEmpty()
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

    override fun onCurrentListChanged(
        previousList: MutableList<CardAndCategory>,
        currentList: MutableList<CardAndCategory>
    ) {
        if (previousList.size < currentList.size) {
            onItemCountIncreased.invoke()
        }
    }
}
