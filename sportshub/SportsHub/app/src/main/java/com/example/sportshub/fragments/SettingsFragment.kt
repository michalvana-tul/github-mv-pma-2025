package com.example.sportshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sportshub.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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

        binding.tvAppVersion.text = "Verze: 1.0.0"
        binding.tvAppInfo.text = """
            SportsHub je aplikace pro správu sportovních výsledků a aktivit.
            
            Funkce:
            • Správa týmových i individuálních sportů
            • Live sledování zápasů s timeline
            • Automatické generování statistik
            • Barevné rozlišení týmů
            • Oblíbené zápasy
            
            Vytvořeno v roce 2026.
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}