package com.example.sportshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportshub.adapters.SportAdapter
import com.example.sportshub.databinding.DialogAddSportBinding
import com.example.sportshub.databinding.FragmentSportsBinding
import com.example.sportshub.models.Sport
import com.example.sportshub.models.SportType
import com.example.sportshub.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class SportsFragment : Fragment() {

    private var _binding: FragmentSportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: SportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeSports()
    }

    private fun setupRecyclerView() {
        adapter = SportAdapter(
            onDeleteClick = { sport ->
                showDeleteDialog(sport)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SportsFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddSport.setOnClickListener {
            showAddSportDialog()
        }
    }

    private fun observeSports() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sports.collect { sports ->
                adapter.submitList(sports)
                binding.tvEmpty.visibility = if (sports.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddSportDialog() {
        val dialogBinding = DialogAddSportBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Přidat nový sport")
            .setView(dialogBinding.root)
            .setPositiveButton("Přidat") { _, _ ->
                val name = dialogBinding.etSportName.text.toString().trim()
                val icon = dialogBinding.etSportIcon.text.toString().trim()
                val type = if (dialogBinding.radioTeam.isChecked) SportType.TEAM else SportType.INDIVIDUAL

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Zadejte název sportu", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val sport = Sport(
                    name = name,
                    type = type,
                    icon = icon.ifEmpty { "⚽" }
                )

                viewModel.addSport(sport) { result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Sport přidán", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showDeleteDialog(sport: Sport) {
        AlertDialog.Builder(requireContext())
            .setTitle("Smazat sport?")
            .setMessage("Opravdu chcete smazat sport ${sport.name}?")
            .setPositiveButton("Smazat") { _, _ ->
                viewModel.deleteSport(sport.id) { result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Sport smazán", Toast.LENGTH_SHORT).show()
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