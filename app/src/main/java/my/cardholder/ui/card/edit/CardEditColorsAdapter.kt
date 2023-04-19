package my.cardholder.ui.card.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemColorBinding

class CardEditColorsAdapter(
    private val onItemClick: (color: String) -> Unit,
) : ListAdapter<String, CardEditColorsAdapter.ColorViewHolder>(ColorDiffCallback) {

    private companion object {
        object ColorDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = true
        }
    }

    inner class ColorViewHolder(
        private val binding: ItemColorBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val color = getItem(adapterPosition)
                onItemClick.invoke(color)
            }
        }

        fun bind(color: String) {
            binding.itemColorImage.setColorFilter(color.toColorInt())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
