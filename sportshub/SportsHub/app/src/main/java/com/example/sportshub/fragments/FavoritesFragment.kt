package com.example.sportshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportshub.adapters.MatchAdapter
import com.example.sportshub.databinding.FragmentFavoritesBinding
import com.example.sportshub.repository.MatchFilter
import com.example.sportshub.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeMatches()

        viewModel.setFilter(MatchFilter.FAVORITES)
    }

    private fun setupRecyclerView() {
        adapter = MatchAdapter(
            onMatchClick = { match ->
                val action = FavoritesFragmentDirections
                    .actionGlobalMatchDetailFragment(match.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { match ->
                viewModel.toggleFavorite(match.id, !match.isFavorite)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }
    }

    private fun observeMatches() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matches.collect { matches ->
                adapter.submitList(matches)
                binding.tvEmpty.visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}