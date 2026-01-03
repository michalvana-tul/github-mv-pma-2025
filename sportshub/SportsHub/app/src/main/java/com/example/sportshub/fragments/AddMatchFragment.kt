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
import androidx.navigation.fragment.findNavController
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

    private var selectedSport: Sport? = null
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

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

        setupSportSpinner()
        setupListeners()
        updateDateTimeDisplay()
    }

    private fun setupSportSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sports.collect { sports ->
                if (sports.isEmpty()) {
                    Toast.makeText(requireContext(), "Nejdřív přidejte sporty!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                    return@collect
                }

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
            }
        }
    }

    private fun updateFormVisibility() {
        val sport = selectedSport ?: return

        if (sport.type == SportType.TEAM) {
            binding.layoutTeamFields.visibility = View.VISIBLE
            binding.layoutIndividualFields.visibility = View.GONE
        } else {
            binding.layoutTeamFields.visibility = View.GONE
            binding.layoutIndividualFields.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSave.setOnClickListener {
            saveMatch()
        }
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDateTime.set(year, month, day)

                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDateTime.set(Calendar.MINUTE, minute)
                        updateDateTimeDisplay()
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
                ).show()
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTimeDisplay() {
        binding.tvSelectedDateTime.text = "📅 ${dateFormat.format(selectedDateTime.time)}"
    }

    private fun saveMatch() {
        val sport = selectedSport ?: return

        if (sport.type == SportType.TEAM) {
            saveTeamMatch(sport)
        } else {
            saveIndividualMatch(sport)
        }
    }

    private fun saveTeamMatch(sport: Sport) {
        val homeTeam = binding.etHomeTeam.text.toString().trim()
        val awayTeam = binding.etAwayTeam.text.toString().trim()
        val homeColor = binding.etHomeColor.text.toString().trim()
        val awayColor = binding.etAwayColor.text.toString().trim()

        if (homeTeam.isEmpty() || awayTeam.isEmpty()) {
            Toast.makeText(requireContext(), "Vyplňte oba týmy", Toast.LENGTH_SHORT).show()
            return
        }

        val match = Match(
            sportId = sport.id,
            sportName = sport.name,
            sportType = sport.type,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeTeamColor = homeColor.ifEmpty { "#6200EE" },
            awayTeamColor = awayColor.ifEmpty { "#03DAC5" },
            date = dateFormat.format(selectedDateTime.time),
            timestamp = selectedDateTime.timeInMillis,
            isLive = binding.cbIsLive.isChecked
        )

        viewModel.addMatch(match) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Zápas přidán", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveIndividualMatch(sport: Sport) {
        val durationStr = binding.etDuration.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (durationStr.isEmpty()) {
            Toast.makeText(requireContext(), "Zadejte délku trvání", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = durationStr.toIntOrNull() ?: 0
        if (duration <= 0) {
            Toast.makeText(requireContext(), "Neplatná délka", Toast.LENGTH_SHORT).show()
            return
        }

        val match = Match(
            sportId = sport.id,
            sportName = sport.name,
            sportType = sport.type,
            duration = duration,
            notes = notes,
            date = dateFormat.format(selectedDateTime.time),
            timestamp = selectedDateTime.timeInMillis,
            isFinished = true
        )

        viewModel.addMatch(match) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Aktivita přidána", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}