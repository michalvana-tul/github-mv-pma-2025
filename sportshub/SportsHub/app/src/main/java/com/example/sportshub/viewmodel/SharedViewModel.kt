package com.example.sportshub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.models.*
import com.example.sportshub.repository.FirebaseRepository
import com.example.sportshub.repository.MatchFilter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    // Sports
    val sports = repository.getSportsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Matches with filter
    private val _currentFilter = MutableStateFlow(MatchFilter.ALL)
    val currentFilter = _currentFilter.asStateFlow()

    val matches = _currentFilter.flatMapLatest { filter ->
        repository.getMatchesFlow(filter)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setFilter(filter: MatchFilter) {
        _currentFilter.value = filter
    }

    // Sport operations
    fun addSport(sport: Sport, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.addSport(sport)
            onResult(result)
        }
    }

    fun deleteSport(sportId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteSport(sportId)
            onResult(result)
        }
    }

    // Match operations
    fun addMatch(match: Match, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.addMatch(match)
            onResult(result)
        }
    }

    fun updateMatch(matchId: String, updates: Map<String, Any>, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateMatch(matchId, updates)
            onResult(result)
        }
    }

    fun deleteMatch(matchId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteMatch(matchId)
            onResult(result)
        }
    }

    fun toggleFavorite(matchId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(matchId, isFavorite)
        }
    }

    fun finishMatch(matchId: String, match: Match, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.finishMatch(matchId, match)
            onResult(result)
        }
    }

    fun addEventToMatch(matchId: String, event: MatchEvent, currentEvents: List<MatchEvent>) {
        val updatedEvents = currentEvents + event
        viewModelScope.launch {
            repository.updateMatch(matchId, mapOf("events" to updatedEvents))
        }
    }
}