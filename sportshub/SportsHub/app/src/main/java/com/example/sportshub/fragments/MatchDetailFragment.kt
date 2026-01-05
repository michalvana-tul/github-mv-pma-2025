package com.example.sportshub.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportshub.R
import com.example.sportshub.adapters.MatchEventAdapter
import com.example.sportshub.databinding.FragmentMatchDetailBinding
import com.example.sportshub.models.*
import com.example.sportshub.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MatchDetailFragment : Fragment() {

    private var _binding: FragmentMatchDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private val args: MatchDetailFragmentArgs by navArgs()
    private lateinit var eventAdapter: MatchEventAdapter
    private var currentMatch: Match? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeMatch()
        setupListeners()
    }

    private fun setupRecyclerView() {
        eventAdapter = MatchEventAdapter()
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun observeMatch() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMatches.collect { matches ->
                val match = matches.find { it.id == args.matchId }
                if (match == null) {
                    if (isAdded) {
                        try { findNavController().navigateUp() } catch (e: Exception) {}
                    }
                    return@collect
                }
                currentMatch = match
                displayMatchData(match)
            }
        }
    }

    private fun displayMatchData(match: Match) {
        val b = _binding ?: return
        val now = System.currentTimeMillis()
        
        // Match is considered "finished" if isFinished is true OR if endTimestamp has passed
        val isMatchTrulyFinished = match.isFinished || match.endTimestamp <= now

        b.apply {
            tvSportName.text = match.sportName
            tvDate.text = match.date

            if (match.sportType == SportType.TEAM) {
                layoutTeamDetail.visibility = View.VISIBLE
                layoutIndividualDetail.visibility = View.GONE
                
                tvHomeTeam.text = match.homeTeam
                tvAwayTeam.text = match.awayTeam
                tvScore.text = "${match.homeScore} : ${match.awayScore}"
                
                try {
                    viewHomeColor.setBackgroundColor(Color.parseColor(match.homeTeamColor))
                    viewAwayColor.setBackgroundColor(Color.parseColor(match.awayTeamColor))
                } catch (e: Exception) {
                    viewHomeColor.setBackgroundColor(Color.GRAY)
                    viewAwayColor.setBackgroundColor(Color.LTGRAY)
                }

                layoutStats.visibility = if (isMatchTrulyFinished) View.VISIBLE else View.GONE
                match.possession?.let {
                    tvPossessionHome.text = "${it["first"]}%"
                    tvPossessionAway.text = "${it["second"]}%"
                    progressPossession.progress = it["first"] ?: 50
                }
                match.shots?.let {
                    tvShotsHome.text = it["first"].toString()
                    tvShotsAway.text = it["second"].toString()
                }

                // Remove finish button if match is finished or timestamp passed
                btnFinishMatch.visibility = if (!isMatchTrulyFinished) View.VISIBLE else View.GONE
                btnEditDuration.visibility = View.GONE
                layoutAddEvent.visibility = if (!isMatchTrulyFinished) View.VISIBLE else View.GONE
                
            } else {
                layoutTeamDetail.visibility = View.GONE
                layoutIndividualDetail.visibility = View.VISIBLE
                
                tvActivityName.text = match.sportName
                tvNotes.text = match.notes.ifEmpty { "Žádné poznámky" }
                
                if (match.duration > 0) {
                    tvDuration.text = "⏱️ Trvání: ${match.duration} min"
                    btnEditDuration.visibility = View.GONE
                    btnFinishMatch.visibility = View.GONE
                } else {
                    tvDuration.text = "⏳ Čas zatím nezadán"
                    btnEditDuration.visibility = View.VISIBLE
                    btnFinishMatch.visibility = View.GONE
                }
                
                layoutStats.visibility = View.GONE
                layoutAddEvent.visibility = View.GONE
            }

            eventAdapter.submitList(match.events.sortedByDescending { it.minute })
            tvNoEvents.visibility = if (match.events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnFinishMatch.setOnClickListener {
            val context = requireContext().applicationContext
            currentMatch?.let { match ->
                viewModel.finishMatch(match.id, match) { result ->
                    activity?.runOnUiThread {
                        if (isAdded) {
                            result.onSuccess {
                                Toast.makeText(context, "Zápas ukončen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding.btnEditDuration.setOnClickListener {
            try {
                val action = MatchDetailFragmentDirections.actionGlobalAddMatchFragment(args.matchId)
                findNavController().navigate(action)
            } catch (e: Exception) {}
        }

        binding.btnAddGoalHome.setOnClickListener { showAddEventDialog(EventType.GOAL, true) }
        binding.btnAddGoalAway.setOnClickListener { showAddEventDialog(EventType.GOAL, false) }
        binding.btnAddCardHome.setOnClickListener { showAddEventDialog(EventType.YELLOW_CARD, true) }
        binding.btnAddCardAway.setOnClickListener { showAddEventDialog(EventType.YELLOW_CARD, false) }
        binding.btnAddRedCardHome.setOnClickListener { showAddEventDialog(EventType.RED_CARD, true) }
        binding.btnAddRedCardAway.setOnClickListener { showAddEventDialog(EventType.RED_CARD, false) }
    }

    private fun showAddEventDialog(type: EventType, isHome: Boolean) {
        val match = currentMatch ?: return
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etMinute = EditText(context).apply {
            hint = "Minuta (např. 45)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val currentMin = ((System.currentTimeMillis() - match.timestamp) / (60 * 1000)).toInt().coerceIn(0, 99)
            setText(currentMin.toString())
        }

        val etPlayer = EditText(context).apply {
            hint = "Jméno hráče"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        layout.addView(etMinute)
        layout.addView(etPlayer)

        val title = when(type) {
            EventType.GOAL -> "⚽ Přidat GÓL - ${if (isHome) match.homeTeam else match.awayTeam}"
            EventType.YELLOW_CARD -> "🟨 Přidat ŽLUTOU KARTU - ${if (isHome) match.homeTeam else match.awayTeam}"
            EventType.RED_CARD -> "🟥 Přidat ČERVENOU KARTU - ${if (isHome) match.homeTeam else match.awayTeam}"
            else -> "Přidat událost"
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("Uložit") { _, _ ->
                val minute = etMinute.text.toString().toIntOrNull() ?: 0
                val player = etPlayer.text.toString().trim().ifEmpty { "Hráč" }
                saveEvent(type, isHome, minute, player)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun saveEvent(type: EventType, isHome: Boolean, minute: Int, playerName: String) {
        val match = currentMatch ?: return
        val event = MatchEvent(
            type = type, minute = minute,
            team = if (isHome) match.homeTeam else match.awayTeam,
            playerName = playerName
        )
        viewModel.addEventToMatch(match.id, event, match.events)
        if (type == EventType.GOAL) {
            val updates = if (isHome) mapOf("homeScore" to match.homeScore + 1) else mapOf("awayScore" to match.awayScore + 1)
            viewModel.updateMatch(match.id, updates)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                try {
                    val action = MatchDetailFragmentDirections.actionGlobalAddMatchFragment(args.matchId)
                    findNavController().navigate(action)
                } catch (e: Exception) {}
                true
            }
            R.id.action_delete -> {
                val appContext = activity?.applicationContext
                Snackbar.make(binding.root, "Opravdu chcete smazat tento zápas?", Snackbar.LENGTH_LONG)
                    .setAction("SMAZAT") {
                        viewModel.deleteMatch(args.matchId) { result ->
                            activity?.runOnUiThread {
                                if (result.isSuccess && isAdded) {
                                    appContext?.let { Toast.makeText(it, "Zápas smazán", Toast.LENGTH_LONG).show() }
                                    try { findNavController().navigateUp() } catch (e: Exception) {}
                                }
                            }
                        }
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
