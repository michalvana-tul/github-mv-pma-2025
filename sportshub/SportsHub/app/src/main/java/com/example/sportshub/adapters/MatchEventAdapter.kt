package com.example.sportshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportshub.databinding.ItemMatchEventBinding
import com.example.sportshub.models.EventType
import com.example.sportshub.models.MatchEvent

class MatchEventAdapter(
    private val onEventOptionsClick: (MatchEvent, View) -> Unit
) : ListAdapter<MatchEvent, MatchEventAdapter.EventViewHolder>(EventDiffCallback()) {

    private var isMatchFinished: Boolean = false

    fun setMatchFinished(finished: Boolean) {
        isMatchFinished = finished
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemMatchEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onEventOptionsClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), isMatchFinished)
    }

    class EventViewHolder(
        private val binding: ItemMatchEventBinding,
        private val onEventOptionsClick: (MatchEvent, View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(event: MatchEvent, isFinished: Boolean) {
            binding.apply {
                btnEventOptions.visibility = if (isFinished) View.GONE else View.VISIBLE
                btnEventOptions.setOnClickListener { onEventOptionsClick(event, it) }
                
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
                
                ivEventType.visibility = View.GONE
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<MatchEvent>() {
        override fun areItemsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem == newItem
        }
    }
}
