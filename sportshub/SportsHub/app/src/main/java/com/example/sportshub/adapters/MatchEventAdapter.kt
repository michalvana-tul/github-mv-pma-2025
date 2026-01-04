package com.example.sportshub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportshub.databinding.ItemMatchEventBinding
import com.example.sportshub.models.EventType
import com.example.sportshub.models.MatchEvent

class MatchEventAdapter : ListAdapter<MatchEvent, MatchEventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemMatchEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(private val binding: ItemMatchEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: MatchEvent) {
            binding.apply {
                tvMinute.text = "${event.minute}'"
                tvPlayerName.text = "${event.playerName} (${event.team})"
                
                when (event.type) {
                    EventType.GOAL -> {
                        tvEventTitle.text = "⚽ GÓL!"
                        tvEventTitle.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    }
                    EventType.YELLOW_CARD -> {
                        tvEventTitle.text = "🟨 ŽLUTÁ KARTA"
                        tvEventTitle.setTextColor(android.graphics.Color.parseColor("#FBC02D"))
                    }
                    EventType.RED_CARD -> {
                        tvEventTitle.text = "🟥 ČERVENÁ KARTA"
                        tvEventTitle.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                    }
                    else -> {
                        tvEventTitle.text = "📝 UDÁLOST"
                        tvEventTitle.setTextColor(android.graphics.Color.GRAY)
                    }
                }
                
                // Schováme ikonu, když používáme emojis v textu (volitelné, pro čistší vzhled)
                ivEventType.visibility = android.view.View.GONE
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<MatchEvent>() {
        override fun areItemsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem.minute == newItem.minute && 
                   oldItem.playerName == newItem.playerName && 
                   oldItem.type == newItem.type
        }
        override fun areContentsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem == newItem
        }
    }
}
