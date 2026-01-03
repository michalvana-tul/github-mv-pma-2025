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
    private val onMatchClick: (Match) -> Unit,
    private val onFavoriteClick: (Match) -> Unit
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
                if (match.sportType == SportType.TEAM) {
                    // Týmový sport
                    tvHomeTeam.text = match.homeTeam
                    tvAwayTeam.text = match.awayTeam
                    tvScore.text = "${match.homeScore} : ${match.awayScore}"

                    // Barvy týmů
                    try {
                        viewHomeColor.setBackgroundColor(Color.parseColor(match.homeTeamColor))
                        viewAwayColor.setBackgroundColor(Color.parseColor(match.awayTeamColor))
                    } catch (e: Exception) {
                        viewHomeColor.setBackgroundColor(Color.GRAY)
                        viewAwayColor.setBackgroundColor(Color.GRAY)
                    }

                    layoutTeamMatch.visibility = View.VISIBLE
                    layoutIndividualMatch.visibility = View.GONE
                } else {
                    // Individuální aktivita
                    tvActivityName.text = match.sportName
                    tvActivityDuration.text = "${match.duration} min"
                    tvActivityNotes.text = match.notes.ifEmpty { "Bez poznámky" }

                    layoutTeamMatch.visibility = View.GONE
                    layoutIndividualMatch.visibility = View.VISIBLE
                }

                // Sport název a datum
                tvSportName.text = match.sportName
                tvDate.text = match.date

                // Status
                when {
                    match.isLive -> {
                        tvStatus.text = "🔴 LIVE"
                        tvStatus.visibility = View.VISIBLE
                    }
                    match.isFinished -> {
                        tvStatus.text = "✓ Ukončeno"
                        tvStatus.visibility = View.VISIBLE
                    }
                    else -> {
                        tvStatus.visibility = View.GONE
                    }
                }

                // Oblíbené
                ivFavorite.setImageResource(
                    if (match.isFavorite) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off
                )

                // Click listeners
                root.setOnClickListener { onMatchClick(match) }
                ivFavorite.setOnClickListener { onFavoriteClick(match) }
            }
        }
    }

    private class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem == newItem
        }
    }
}