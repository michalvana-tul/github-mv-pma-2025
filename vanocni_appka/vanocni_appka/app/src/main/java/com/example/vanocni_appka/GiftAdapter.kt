package com.example.vanocni_appka

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vanocni_appka.data.Gift
import com.example.vanocni_appka.databinding.ItemGiftBinding

class GiftAdapter(
    private val onEdit: (Gift) -> Unit,
    private val onDelete: (Gift) -> Unit
) : ListAdapter<Gift, GiftAdapter.GiftViewHolder>(DiffCallback) {

    class GiftViewHolder(val binding: ItemGiftBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(ItemGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val gift = getItem(position)
        holder.binding.apply {
            tvGiftName.text = gift.name
            tvGiftPerson.text = "Pro: ${gift.person}"
            tvGiftStatus.text = if (gift.isBought) "✅ Koupeno" else "⏳ Čeká"
            
            if (!gift.imageUri.isNullOrEmpty()) {
                ivGiftImage.setImageURI(Uri.parse(gift.imageUri))
            } else {
                ivGiftImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            root.setOnClickListener { onEdit(gift) }
            btnDelete.setOnClickListener { onDelete(gift) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Gift>() {
        override fun areItemsTheSame(oldItem: Gift, newItem: Gift) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Gift, newItem: Gift) = oldItem == newItem
    }
}
