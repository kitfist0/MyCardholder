package my.cardholder.ui.card.edit

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import my.cardholder.R

class CardEditColorAdapter(
    context: Context,
    private val items: List<String>,
) : ArrayAdapter<String>(context, R.layout.item_color, items) {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): String {
        return items[position]
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val itemView = view
            ?: LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return itemView.apply {
            val color = getItem(position)
            findViewById<TextView>(R.id.item_color_text).apply {
                text = color
                TextViewCompat.setCompoundDrawableTintList(
                    this,
                    ColorStateList.valueOf(color.toColorInt())
                )
            }
        }
    }
}
