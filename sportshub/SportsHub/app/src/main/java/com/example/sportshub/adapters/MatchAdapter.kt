package com.example.sportshub.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportshub.databinding.ItemMatchBinding
import com.example.sportshub.models.Match
import com.example.sportshub.models.SportType

class MatchAdapter(
    private val onMatchClick: (Match) -> Unit
) : ListAdapter<Match, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(
        private val binding: ItemMatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.apply {
                tvSportName.text = match.sportName
                tvDate.text = match.date

                if (match.sportType == SportType.TEAM) {
                    tvHomeTeam.text = match.homeTeam
                    tvAwayTeam.text = match.awayTeam
                    tvScore.text = "${match.homeScore} : ${match.awayScore}"

                    try {
                        val hColor = if (match.homeTeamColor.isNullOrEmpty() || !match.homeTeamColor.startsWith("#")) "#6200EE" else match.homeTeamColor
                        val aColor = if (match.awayTeamColor.isNullOrEmpty() || !match.awayTeamColor.startsWith("#")) "#03DAC5" else match.awayTeamColor
                        viewHomeColor.setBackgroundColor(Color.parseColor(hColor))
                        viewAwayColor.setBackgroundColor(Color.parseColor(aColor))
                    } catch (e: Exception) {
                        viewHomeColor.setBackgroundColor(Color.GRAY)
                        viewAwayColor.setBackgroundColor(Color.GRAY)
                    }

                    layoutTeamMatch.visibility = View.VISIBLE
                    layoutIndividualMatch.visibility = View.GONE
                    
                    val statusVisible = match.isLive || match.isFinished
                    tvStatus.visibility = if (statusVisible) View.VISIBLE else View.GONE
                    tvStatus.text = if (match.isLive) "🔴 LIVE" else "✓ Ukončeno"
                    layoutAddTime.visibility = View.GONE

                } else {
                    layoutTeamMatch.visibility = View.GONE
                    layoutIndividualMatch.visibility = View.VISIBLE
                    
                    tvActivityName.text = match.sportName
                    tvActivityNotes.text = match.notes.ifEmpty { "Bez poznámky" }

                    if (match.duration == 0) {
                        tvActivityDuration.visibility = View.GONE
                        layoutAddTime.visibility = View.VISIBLE
                        tvStatus.visibility = View.GONE
                    } else {
                        tvActivityDuration.visibility = View.VISIBLE
                        tvActivityDuration.text = "⏱️ ${match.duration} min"
                        layoutAddTime.visibility = View.GONE
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = "✓ Hotovo"
                    }
                }

                root.setOnClickListener { onMatchClick(match) }
            }
        }
    }

    private class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Match, newItem: Match) = oldItem == newItem
    }
}
