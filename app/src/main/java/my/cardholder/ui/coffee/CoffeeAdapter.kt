// package my.cardholder.ui.coffee

// import android.view.LayoutInflater
// import android.view.ViewGroup
// import androidx.recyclerview.widget.DiffUtil
// import androidx.recyclerview.widget.ListAdapter
// import androidx.recyclerview.widget.RecyclerView
// import my.cardholder.R
// import my.cardholder.data.model.Coffee
// import my.cardholder.databinding.ItemCoffeeBinding
// import my.cardholder.util.ext.setStartEndCompoundDrawables

// class CoffeeAdapter(
//     private val onItemClick: (productId: String) -> Unit,
// ) : ListAdapter<Coffee, CoffeeAdapter.CoffeeViewHolder>(CoffeeDiffCallback) {

//     private companion object {
//         object CoffeeDiffCallback : DiffUtil.ItemCallback<Coffee>() {
//             override fun areItemsTheSame(oldItem: Coffee, newItem: Coffee) =
//                 oldItem.id == newItem.id

//             override fun areContentsTheSame(oldItem: Coffee, newItem: Coffee) =
//                 oldItem.isPurchased == newItem.isPurchased
//         }
//     }

//     inner class CoffeeViewHolder(
//         private val binding: ItemCoffeeBinding,
//     ) : RecyclerView.ViewHolder(binding.root) {

//         init {
//             itemView.setOnClickListener {
//                 val coffee = getItem(adapterPosition)
//                 onItemClick.invoke(coffee.id)
//             }
//         }

//         fun bind(coffee: Coffee) {
//             binding.itemCoffeeText.apply {
//                 text = coffee.name
//                 setStartEndCompoundDrawables(
//                     startDrawableResId = if (coffee.isPurchased) {
//                         R.drawable.ic_product_purchased
//                     } else {
//                         R.drawable.ic_product_default
//                     }
//                 )
//             }
//         }
//     }

//     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
//         val binding = ItemCoffeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//         return CoffeeViewHolder(binding)
//     }

//     override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
//         holder.bind(getItem(position))
//     }
// }
