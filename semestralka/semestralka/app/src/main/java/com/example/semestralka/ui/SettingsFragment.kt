package com.example.semestralka.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.semestralka.data.AppDatabase
import com.example.semestralka.data.UserPreferencesRepository
import com.example.semestralka.databinding.FragmentSettingsBinding
import com.example.semestralka.viewmodel.BookViewModel
import com.example.semestralka.viewmodel.BookViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = UserPreferencesRepository(requireContext())
        BookViewModelFactory(database.bookDao(), repository)
    }

    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences: User Name
        val savedName = sharedPrefs.getString("user_name", "User")
        binding.tvUserNameDisplay.text = "Welcome, $savedName!"

        binding.btnSaveUserName.setOnClickListener {
            val newName = binding.etUserName.text.toString()
            if (newName.isNotBlank()) {
                sharedPrefs.edit().putString("user_name", newName).apply()
                binding.tvUserNameDisplay.text = "Welcome, $newName!"
                binding.etUserName.text.clear()
            }
        }

        // DataStore: Hide Read Books
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hideReadBooks.collectLatest { isChecked ->
                    binding.switchHideRead.isChecked = isChecked
                }
            }
        }

        binding.switchHideRead.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateHideReadBooks(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
