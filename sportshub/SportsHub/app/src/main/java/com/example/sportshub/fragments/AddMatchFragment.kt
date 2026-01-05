package com.example.sportshub.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sportshub.R
import com.example.sportshub.databinding.FragmentAddMatchBinding
import com.example.sportshub.models.Match
import com.example.sportshub.models.Sport
import com.example.sportshub.models.SportType
import com.example.sportshub.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMatchFragment : Fragment() {

    private var _binding: FragmentAddMatchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private val args: AddMatchFragmentArgs by navArgs()

    private var selectedSport: Sport? = null
    private var allSports: List<Sport> = emptyList()
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isEditMode = args.matchId.isNotEmpty()
        if (isEditMode) {
            binding.btnSave.text = "Uložit změny"
        }

        setupSportSpinner()
        setupListeners()
        updateDateTimeDisplay()
    }

    private fun setupSportSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sports.collect { sports ->
                allSports = sports
                if (sports.isEmpty()) return@collect

                val sportNames = sports.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sportNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSport.adapter = adapter

                binding.spinnerSport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedSport = sports[position]
                        updateFormVisibility()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                if (isEditMode) loadMatchData()
            }
        }
    }

    private fun loadMatchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMatches.collect { matches ->
                val match = matches.find { it.id == args.matchId } ?: return@collect
                
                val sportIndex = allSports.indexOfFirst { it.id == match.sportId }
                if (sportIndex != -1) binding.spinnerSport.setSelection(sportIndex)

                selectedDateTime.timeInMillis = match.timestamp
                updateDateTimeDisplay()

                if (match.sportType == SportType.TEAM) {
                    binding.etHomeTeam.setText(match.homeTeam)
                    binding.etAwayTeam.setText(match.awayTeam)
                    binding.etHomeColor.setText(match.homeTeamColor)
                    binding.etAwayColor.setText(match.awayTeamColor)
                    val durationMin = (match.endTimestamp - match.timestamp) / (60 * 1000)
                    binding.etMatchDuration.setText(durationMin.toString())
                } else {
                    binding.etDuration.setText(if (match.duration > 0) match.duration.toString() else "")
                    binding.etNotes.setText(match.notes)
                }
                updateFormVisibility()
            }
        }
    }

    private fun updateFormVisibility() {
        val b = _binding ?: return
        val sport = selectedSport ?: return
        if (sport.type == SportType.TEAM) {
            b.layoutTeamFields.visibility = View.VISIBLE
            b.layoutIndividualFields.visibility = View.GONE
        } else {
            b.layoutTeamFields.visibility = View.GONE
            b.layoutIndividualFields.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnSelectDateTime.setOnClickListener { showDateTimePicker() }
        binding.btnSave.setOnClickListener { saveMatch() }
    }

    private fun showDateTimePicker() {
        DatePickerDialog(requireContext(), { _, year, month, day ->
            selectedDateTime.set(year, month, day)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateTime.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show()
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateTimeDisplay() {
        binding.tvSelectedDateTime.text = "📅 ${dateFormat.format(selectedDateTime.time)}"
    }

    private fun saveMatch() {
        val b = _binding ?: return
        val sport = selectedSport ?: return
        val startTs = selectedDateTime.timeInMillis

        b.btnSave.isEnabled = false

        if (sport.type == SportType.TEAM) {
            val home = b.etHomeTeam.text.toString().trim()
            val away = b.etAwayTeam.text.toString().trim()
            val durationMin = b.etMatchDuration.text.toString().toIntOrNull() ?: 90
            val endTs = startTs + (durationMin * 60 * 1000)

            if (home.isEmpty() || away.isEmpty()) {
                b.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Zadejte názvy týmů", Toast.LENGTH_SHORT).show()
                return
            }

            val updates = mutableMapOf<String, Any>(
                "sportId" to sport.id,
                "sportName" to sport.name,
                "homeTeam" to home,
                "awayTeam" to away,
                "homeTeamColor" to b.etHomeColor.text.toString().trim().ifEmpty { "#6200EE" },
                "awayTeamColor" to b.etAwayColor.text.toString().trim().ifEmpty { "#03DAC5" },
                "date" to dateFormat.format(selectedDateTime.time),
                "timestamp" to startTs,
                "endTimestamp" to endTs,
                "isLive" to false
            )

            if (isEditMode) {
                viewModel.updateMatch(args.matchId, updates) { result -> handleResult(result) }
            } else {
                val match = Match(
                    sportId = sport.id, sportName = sport.name, sportType = sport.type,
                    homeTeam = home, awayTeam = away,
                    homeTeamColor = updates["homeTeamColor"] as String,
                    awayTeamColor = updates["awayTeamColor"] as String,
                    date = updates["date"] as String,
                    timestamp = startTs,
                    endTimestamp = endTs,
                    isLive = false
                )
                viewModel.addMatch(match) { result -> handleResult(result) }
            }
        } else {
            val dur = b.etDuration.text.toString().trim().toIntOrNull() ?: 0
            val endTs = startTs + (dur * 60 * 1000)
            val isFinished = dur > 0

            val updates = mutableMapOf<String, Any>(
                "sportId" to sport.id,
                "sportName" to sport.name,
                "duration" to dur,
                "notes" to b.etNotes.text.toString().trim(),
                "date" to dateFormat.format(selectedDateTime.time),
                "timestamp" to startTs,
                "endTimestamp" to endTs,
                "isFinished" to isFinished,
                "isLive" to false
            )

            if (isEditMode) {
                viewModel.updateMatch(args.matchId, updates) { result -> handleResult(result) }
            } else {
                val match = Match(
                    sportId = sport.id, sportName = sport.name, sportType = sport.type,
                    duration = dur, notes = b.etNotes.text.toString().trim(),
                    date = dateFormat.format(selectedDateTime.time),
                    timestamp = startTs,
                    endTimestamp = endTs,
                    isFinished = isFinished,
                    isLive = false
                )
                viewModel.addMatch(match) { result -> handleResult(result) }
            }
        }
    }

    private fun handleResult(result: Result<*>) {
        if (!isAdded) return
        
        activity?.runOnUiThread {
            if (result.isSuccess) {
                Toast.makeText(requireContext(), if (isEditMode) "Upraveno" else "Uloženo", Toast.LENGTH_SHORT).show()
                
                // Navigate to programFragment and clear the backstack
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                
                findNavController().navigate(R.id.programFragment, null, navOptions)
            } else {
                binding.btnSave.isEnabled = true
                val error = result.exceptionOrNull()?.message ?: "Neznámá chyba"
                Toast.makeText(requireContext(), "Chyba: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
