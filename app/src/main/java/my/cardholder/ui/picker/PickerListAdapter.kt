package my.cardholder.ui.picker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.databinding.ItemColorBinding

class PickerListAdapter(
    private val colors: List<String>,
    private val onItemClick: (color: String) -> Unit,
) : RecyclerView.Adapter<PickerListAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(
        private val binding: ItemColorBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val color = colors[adapterPosition]
                onItemClick.invoke(color)
            }
        }

        fun bind(color: String) {
            with(binding) {
                itemColorText.text = color
                itemColorCard.setCardBackgroundColor(color.toColorInt())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int {
        return colors.size
    }
}
