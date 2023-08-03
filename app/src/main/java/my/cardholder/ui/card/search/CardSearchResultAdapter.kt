package my.cardholder.ui.card.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.isSquare
import my.cardholder.databinding.ItemSearchResultBinding
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.toNavExtras

class CardSearchResultAdapter(
    private val onItemClicked: (cardId: Long, navExtras: FragmentNavigator.Extras) -> Unit,
) : ListAdapter<Card, CardSearchResultAdapter.SearchViewHolder>(CardDiffCallback) {

    private companion object {
        object CardDiffCallback : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Card, newItem: Card) =
                oldItem == newItem
        }
    }

    inner class SearchViewHolder(
        private val binding: ItemSearchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val card = getItem(adapterPosition)
                val extras = listOf(
                    binding.itemSearchResultBarcodeImage,
                    binding.itemSearchResultNameText,
                    binding.itemSearchResultContentText,
                ).toNavExtras()
                onItemClicked.invoke(card.id, extras)
            }
        }

        fun bind(card: Card) {
            with(binding) {
                val uniqueNameSuffix = card.id
                itemSearchResultCard.setCardBackgroundColor(card.getColorInt())
                itemSearchResultBarcodeImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    setImageResource(
                        if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode
                    )
                }
                itemSearchResultNameText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.name
                }
                itemSearchResultContentText.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    text = card.content
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
