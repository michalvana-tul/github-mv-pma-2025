package com.example.sportshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sportshub.databinding.FragmentSettingsBinding
import com.example.sportshub.viewmodel.SharedViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        updateAppInfo()
    }

    private fun setupListeners() {
        binding.btnClearMatches.setOnClickListener {
            val context = requireContext().applicationContext
            AlertDialog.Builder(requireContext())
                .setTitle("Smazat všechna data")
                .setMessage("Opravdu chcete smazat všechny zápasy?")
                .setPositiveButton("Smazat vše") { _, _ ->
                    viewModel.deleteAllMatches { result ->
                        activity?.runOnUiThread {
                            if (result.isSuccess) {
                                Toast.makeText(context, "Všechna data byla smazána", Toast.LENGTH_LONG).show()
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Neznámá chyba"
                                Toast.makeText(context, "Chyba: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Zrušit", null)
                .show()
        }
    }

    private fun updateAppInfo() {
        binding.tvAppVersion.text = "Verze: 1.0.0"
        binding.tvAppInfo.text = "SportsHub - aplikace pro správu sportovních aktivit"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
