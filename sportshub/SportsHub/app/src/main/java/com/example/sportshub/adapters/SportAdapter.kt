package com.example.sportshub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportshub.databinding.ItemSportBinding
import com.example.sportshub.models.Sport

class SportAdapter(
    private val onDeleteClick: (Sport) -> Unit
) : ListAdapter<Sport, SportAdapter.SportViewHolder>(SportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportViewHolder {
        val binding = ItemSportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SportViewHolder(
        private val binding: ItemSportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sport: Sport) {
            binding.apply {
                tvSportName.text = sport.name
                tvSportType.text = if (sport.type.name == "TEAM") "Týmový sport" else "Individuální"
                tvSportIcon.text = sport.icon.ifEmpty { "⚽" }

                btnDelete.setOnClickListener { onDeleteClick(sport) }
            }
        }
    }

    private class SportDiffCallback : DiffUtil.ItemCallback<Sport>() {
        override fun areItemsTheSame(oldItem: Sport, newItem: Sport): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Sport, newItem: Sport): Boolean {
            return oldItem == newItem
        }
    }
}