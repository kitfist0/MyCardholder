package my.cardholder.ui.card.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
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
        const val COMPACT_SPAN_COUNT = 3

        object CardDiffCallback : DiffUtil.ItemCallback<CardAndCategory>() {
            override fun areItemsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem.card.id == newItem.card.id

            override fun areContentsTheSame(oldItem: CardAndCategory, newItem: CardAndCategory) =
                oldItem == newItem
        }
    }

    /**
     * In a single-column list cards overlap, so the category is moved to the top of the card.
     * In a three-column list cards are too narrow, so they show only the logo and the name.
     */
    var spanCount = 1
        set(value) {
            if (field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }

    private var touchHelper: ItemTouchHelper? = null
    private var draggedItemView: View? = null

    fun isDragged(itemView: View): Boolean = itemView === draggedItemView

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

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    draggedItemView = viewHolder?.itemView
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                draggedItemView = null
            }

            override fun isLongPressDragEnabled() = true
        }

        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(recyclerView)
    }

    inner class CardViewHolder(
        private val binding: ItemCardBinding,
        private val onDragStart: (RecyclerView.ViewHolder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                if (itemView.isPressed) {
                    val cardAndCategory = getItem(adapterPosition)
                    val extras = listOf(
                        binding.itemCardLogoImage,
                        binding.itemCardNameText,
                        binding.itemCardContentText,
                        binding.itemCardCategoryText,
                    ).filter { it.isVisible }.toNavExtras()
                    onItemClicked.invoke(cardAndCategory.card.id, extras)
                }
            }
            itemView.setOnLongClickListener {
                onDragStart(this)
                true
            }
        }

        fun bind(cardAndCategory: CardAndCategory) {
            with(binding) {
                val card = cardAndCategory.card
                val uniqueNameSuffix = card.id
                itemCardLayout.background = getCardGradientDrawable(card.getColorInt())
                itemCardLogoImage.apply {
                    setupUniqueTransitionName(uniqueNameSuffix)
                    loadLogoImage(
                        logoUrl = card.logo,
                        defaultDrawableRes = if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode,
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
                applyCompactLayout(isCompact = spanCount >= COMPACT_SPAN_COUNT)
                placeCategory(atTop = spanCount == 1)
            }
        }

        private fun applyCompactLayout(isCompact: Boolean) {
            val resources = binding.root.resources
            with(binding) {
                itemCardContentText.isVisible = !isCompact
                itemCardCategoryText.isVisible = !isCompact
                val padding = resources.getDimensionPixelSize(
                    if (isCompact) R.dimen.card_item_compact_content_padding else R.dimen.card_item_content_padding
                )
                itemCardLayout.setPadding(padding, padding, padding, padding)
                val logoSize = resources.getDimensionPixelSize(
                    if (isCompact) R.dimen.card_item_compact_logo_size else R.dimen.card_item_square_logo_size
                )
                itemCardLogoImage.updateLayoutParams {
                    width = logoSize
                    height = logoSize
                }
                TextViewCompat.setTextAppearance(
                    itemCardNameText,
                    if (isCompact) {
                        com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
                    } else {
                        com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall
                    }
                )
            }
        }

        private fun placeCategory(atTop: Boolean) {
            val resources = binding.root.resources
            binding.itemCardCategoryText.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (atTop) {
                        // Next to the logo, in the top end corner.
                        topToBottom = ConstraintLayout.LayoutParams.UNSET
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        startToStart = ConstraintLayout.LayoutParams.UNSET
                        startToEnd = binding.itemCardLogoImage.id
                        marginStart = resources.getDimensionPixelSize(R.dimen.card_item_category_logo_spacing)
                    } else {
                        // Below the content, at the end.
                        topToTop = ConstraintLayout.LayoutParams.UNSET
                        topToBottom = binding.itemCardContentText.id
                        startToEnd = ConstraintLayout.LayoutParams.UNSET
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        marginStart = 0
                    }
                }
                updatePadding(
                    top = if (atTop) 0 else resources.getDimensionPixelSize(R.dimen.card_item_category_padding_top)
                )
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
