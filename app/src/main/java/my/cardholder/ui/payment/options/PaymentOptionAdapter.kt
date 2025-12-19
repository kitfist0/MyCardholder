package my.cardholder.ui.payment.options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R
import my.cardholder.data.model.PaymentOption
import my.cardholder.data.model.PaymentOption.Companion.getTitleStringRes
import my.cardholder.databinding.ItemPaymentOptionBinding
import my.cardholder.util.ext.setStartEndCompoundDrawables

class PaymentOptionAdapter(
    private val onItemClicked: (paymentOption: PaymentOption) -> Unit,
) : ListAdapter<PaymentOptionState, PaymentOptionAdapter.ItemViewHolder>(PaymentOptionStateDiffCallback) {

    private companion object {
        object PaymentOptionStateDiffCallback : DiffUtil.ItemCallback<PaymentOptionState>() {
            override fun areItemsTheSame(oldItem: PaymentOptionState, newItem: PaymentOptionState) =
                oldItem.paymentOption == newItem.paymentOption

            override fun areContentsTheSame(oldItem: PaymentOptionState, newItem: PaymentOptionState) =
                oldItem.isSelected == newItem.isSelected
        }
    }

    inner class ItemViewHolder(
        private val binding: ItemPaymentOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val paymentOptionState = getItem(adapterPosition)
                onItemClicked.invoke(paymentOptionState.paymentOption)
            }
        }

        fun bind(paymentOptionState: PaymentOptionState) {
            binding.itemPaymentOptionText.apply {
                text = context.getString(paymentOptionState.paymentOption.getTitleStringRes())
                    .format(paymentOptionState.costOfPayment)
                setStartEndCompoundDrawables(
                    endDrawableResId = if (paymentOptionState.isSelected) R.drawable.ic_done else null
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemPaymentOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
