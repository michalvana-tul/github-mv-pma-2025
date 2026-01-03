package com.example.sportshub.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportshub.adapters.EventAdapter
import com.example.sportshub.databinding.DialogAddEventBinding
import com.example.sportshub.databinding.FragmentMatchDetailBinding
import com.example.sportshub.models.EventType
import com.example.sportshub.models.Match
import com.example.sportshub.models.MatchEvent
import com.example.sportshub.models.SportType
import com.example.sportshub.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class MatchDetailFragment : Fragment() {

    private var _binding: FragmentMatchDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private val args: MatchDetailFragmentArgs by navArgs()

    private lateinit var eventAdapter: EventAdapter
    private var currentMatch: Match? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEventRecyclerView()
        setupListeners()
        observeMatch()
    }

    private fun setupEventRecyclerView() {
        eventAdapter = EventAdapter()
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        binding.btnFinishMatch.setOnClickListener {
            finishMatch()
        }

        binding.btnDelete.setOnClickListener {
            deleteMatch()
        }

        binding.btnIncreaseHome.setOnClickListener {
            updateScore(isHome = true, increment = 1)
        }

        binding.btnDecreaseHome.setOnClickListener {
            updateScore(isHome = true, increment = -1)
        }

        binding.btnIncreaseAway.setOnClickListener {
            updateScore(isHome = false, increment = 1)
        }

        binding.btnDecreaseAway.setOnClickListener {
            updateScore(isHome = false, increment = -1)
        }
    }

    private fun observeMatch() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matches.collect { matches ->
                val match = matches.find { it.id == args.matchId }
                if (match != null) {
                    currentMatch = match
                    displayMatch(match)
                }
            }
        }
    }

    private fun displayMatch(match: Match) {
        binding.apply {
            if (match.sportType == SportType.TEAM) {
                // Týmový zápas
                layoutTeamMatch.visibility = View.VISIBLE
                layoutIndividualMatch.visibility = View.GONE
                layoutScoreControls.visibility = if (match.isLive) View.VISIBLE else View.GONE
                layoutStats.visibility = if (match.isFinished && match.possession != null) View.VISIBLE else View.GONE

                tvHomeTeam.text = match.homeTeam
                tvAwayTeam.text = match.awayTeam
                tvHomeScore.text = match.homeScore.toString()
                tvAwayScore.text = match.awayScore.toString()

                // Barvy týmů pro hlavičku
                try {
                    val homeColor = Color.parseColor(match.homeTeamColor)
                    val awayColor = Color.parseColor(match.awayTeamColor)

                    cardHeader.setCardBackgroundColor(
                        blendColors(homeColor, awayColor)
                    )
                } catch (e: Exception) {
                    cardHeader.setCardBackgroundColor(Color.GRAY)
                }

                // Live minuta - timeline
                if (match.isLive) {
                    layoutTimeline.visibility = View.VISIBLE
                    btnAddEvent.visibility = View.VISIBLE
                    btnFinishMatch.visibility = View.VISIBLE
                    eventAdapter.submitList(match.events.sortedByDescending { it.minute })
                } else if (match.isFinished && match.events.isNotEmpty()) {
                    layoutTimeline.visibility = View.VISIBLE
                    btnAddEvent.visibility = View.GONE
                    btnFinishMatch.visibility = View.GONE
                    eventAdapter.submitList(match.events.sortedByDescending { it.minute })
                } else {
                    layoutTimeline.visibility = View.GONE
                    btnAddEvent.visibility = View.GONE
                    btnFinishMatch.visibility = View.GONE
                }

                // Statistiky (Random Stats)
                if (match.isFinished && match.possession != null && match.shots != null) {
                    val possession = match.possession!!
                    val shots = match.shots!!

                    tvPossessionHome.text = "${possession.first}%"
                    tvPossessionAway.text = "${possession.second}%"
                    progressPossession.progress = possession.first

                    tvShotsHome.text = shots.first.toString()
                    tvShotsAway.text = shots.second.toString()

                    val totalShots = shots.first + shots.second
                    if (totalShots > 0) {
                        progressShots.progress = (shots.first * 100 / totalShots)
                    }
                }

            } else {
                // Individuální aktivita
                layoutTeamMatch.visibility = View.GONE
                layoutIndividualMatch.visibility = View.VISIBLE
                layoutTimeline.visibility = View.GONE
                layoutScoreControls.visibility = View.GONE
                layoutStats.visibility = View.GONE

                tvActivityName.text = match.sportName
                tvActivityDuration.text = "⏱️ ${match.duration} minut"
                tvActivityNotes.text = if (match.notes.isNotEmpty()) {
                    "📝 ${match.notes}"
                } else {
                    "Bez poznámky"
                }
            }

            // Status
            tvStatus.text = when {
                match.isLive -> "🔴 ŽIVĚ"
                match.isFinished -> "✅ Ukončeno"
                else -> "📅 Plánováno"
            }

            tvDate.text = match.date
        }
    }

    private fun blendColors(color1: Int, color2: Int): Int {
        val r = (Color.red(color1) + Color.red(color2)) / 2
        val g = (Color.green(color1) + Color.green(color2)) / 2
        val b = (Color.blue(color1) + Color.blue(color2)) / 2
        return Color.rgb(r, g, b)
    }

    private fun updateScore(isHome: Boolean, increment: Int) {
        val match = currentMatch ?: return
        if (!match.isLive) {
            Toast.makeText(requireContext(), "Zápas není živý", Toast.LENGTH_SHORT).show()
            return
        }

        val newHomeScore = if (isHome) (match.homeScore + increment).coerceAtLeast(0) else match.homeScore
        val newAwayScore = if (!isHome) (match.awayScore + increment).coerceAtLeast(0) else match.awayScore

        val updates = mapOf(
            "homeScore" to newHomeScore,
            "awayScore" to newAwayScore
        )

        viewModel.updateMatch(match.id, updates) { result ->
            result.onFailure { e ->
                Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddEventDialog() {
        val match = currentMatch ?: return
        if (!match.isLive) return

        val dialogBinding = DialogAddEventBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Přidat událost")
            .setView(dialogBinding.root)
            .setPositiveButton("Přidat") { _, _ ->
                val minuteStr = dialogBinding.etMinute.text.toString().trim()
                val playerName = dialogBinding.etPlayerName.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()

                if (minuteStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Zadejte minutu", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val minute = minuteStr.toIntOrNull() ?: 0

                val eventType = when (dialogBinding.radioGroupEventType.checkedRadioButtonId) {
                    dialogBinding.radioGoal.id -> EventType.GOAL
                    dialogBinding.radioYellowCard.id -> EventType.YELLOW_CARD
                    dialogBinding.radioRedCard.id -> EventType.RED_CARD
                    dialogBinding.radioSubstitution.id -> EventType.SUBSTITUTION
                    else -> EventType.OTHER
                }

                val team = if (dialogBinding.radioHome.isChecked) "home" else "away"

                val event = MatchEvent(
                    minute = minute,
                    type = eventType,
                    team = team,
                    playerName = playerName,
                    description = description
                )

                viewModel.addEventToMatch(match.id, event, match.events)
                Toast.makeText(requireContext(), "Událost přidána", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun finishMatch() {
        val match = currentMatch ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Ukončit zápas?")
            .setMessage("Zápas bude ukončen a vygenerují se statistiky.")
            .setPositiveButton("Ukončit") { _, _ ->
                viewModel.finishMatch(match.id, match) { result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Zápas ukončen", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun deleteMatch() {
        val match = currentMatch ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Smazat zápas?")
            .setMessage("Opravdu chcete smazat tento zápas?")
            .setPositiveButton("Smazat") { _, _ ->
                viewModel.deleteMatch(match.id) { result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Zápas smazán", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
