package com.example.sportshub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportshub.databinding.ItemEventBinding
import com.example.sportshub.models.EventType
import com.example.sportshub.models.MatchEvent

class EventAdapter : ListAdapter<MatchEvent, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: MatchEvent) {
            binding.apply {
                tvMinute.text = "${event.minute}'"

                val icon = when (event.type) {
                    EventType.GOAL -> "⚽"
                    EventType.YELLOW_CARD -> "🟨"
                    EventType.RED_CARD -> "🟥"
                    EventType.SUBSTITUTION -> "🔄"
                    EventType.OTHER -> "📝"
                }

                val typeText = when (event.type) {
                    EventType.GOAL -> "Gól"
                    EventType.YELLOW_CARD -> "Žlutá karta"
                    EventType.RED_CARD -> "Červená karta"
                    EventType.SUBSTITUTION -> "Střídání"
                    EventType.OTHER -> "Jiné"
                }

                tvEventIcon.text = icon
                tvEventType.text = typeText

                val teamText = if (event.team == "home") "Domácí" else "Hosté"
                tvEventTeam.text = teamText

                if (event.playerName.isNotEmpty()) {
                    tvEventPlayer.text = event.playerName
                } else {
                    tvEventPlayer.text = ""
                }

                if (event.description.isNotEmpty()) {
                    tvEventDescription.text = event.description
                } else {
                    tvEventDescription.text = ""
                }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<MatchEvent>() {
        override fun areItemsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem.minute == newItem.minute && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: MatchEvent, newItem: MatchEvent): Boolean {
            return oldItem == newItem
        }
    }
}