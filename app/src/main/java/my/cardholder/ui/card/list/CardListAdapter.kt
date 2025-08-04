package my.cardholder.ui.card.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.isSquare
import my.cardholder.databinding.ItemCardBinding
import my.cardholder.util.ext.loadLogoImage
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.toNavExtras
import java.util.Collections

class CardListAdapter(
    private val onItemClicked: (cardId: Long, navExtras: FragmentNavigator.Extras) -> Unit,
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

    private var touchHelper: ItemTouchHelper? = null

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                val items = currentList.toMutableList()
                Collections.swap(items, fromPos, toPos)
                submitList(items)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled() = true
        }

        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(recyclerView)
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class CardViewHolder(
        private val binding: ItemCardBinding,
        private val onDragStart: (RecyclerView.ViewHolder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val cardAndCategory = getItem(adapterPosition)
                val extras = listOf(
                    binding.itemCardLogoImage,
                    binding.itemCardNameText,
                    binding.itemCardContentText,
                    binding.itemCardCategoryText,
                ).toNavExtras()
                onItemClicked.invoke(cardAndCategory.card.id, extras)
            }
            itemView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onDragStart(this)
                }
                false
            }
        }

        fun bind(cardAndCategory: CardAndCategory) {
            with(binding) {
                val card = cardAndCategory.card
                val uniqueNameSuffix = card.id
                itemCardLayout.background = getCardGradientDrawable(card.getColorInt())
                itemCardLogoImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    card.logo
                        ?.let { loadLogoImage(it) }
                        ?: setImageResource(
                            if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode
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
            return GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(bottomLeftColor, colorInt)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding) { holder ->
            touchHelper?.startDrag(holder)
        }
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
