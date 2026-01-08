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

    val sports = repository.getSportsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allMatchesFlow = repository.getMatchesFlow(MatchFilter.ALL)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMatches = allMatchesFlow

    private val _currentFilter = MutableStateFlow(MatchFilter.ALL)
    val currentFilter = _currentFilter.asStateFlow()

    val matches = combine(allMatchesFlow, _currentFilter) { list, filter ->
        val now = System.currentTimeMillis()
        when (filter) {
            MatchFilter.UPCOMING -> list.filter { 
                if (it.sportType == SportType.INDIVIDUAL) it.duration == 0 
                else !it.isFinished && it.endTimestamp > now 
            }
            MatchFilter.FINISHED -> list.filter { 
                if (it.sportType == SportType.INDIVIDUAL) it.duration > 0 
                else it.isFinished || it.endTimestamp <= now 
            }
            MatchFilter.LIVE -> list.filter { 
                it.sportType == SportType.TEAM && 
                (it.isLive || (it.timestamp <= now && it.endTimestamp > now)) && !it.isFinished 
            }
            MatchFilter.ALL -> list
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(filter: MatchFilter) {
        _currentFilter.value = filter
    }

    fun addMatch(match: Match, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.addMatch(match)
            onResult(result)
        }
    }

    fun updateMatch(matchId: String, updates: Map<String, Any>, onResult: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.updateMatch(matchId, updates)
            onResult?.invoke(result)
        }
    }

    fun deleteMatch(matchId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteMatch(matchId)
            onResult(result)
        }
    }

    fun deleteAllMatches(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAllMatches()
            onResult(result)
        }
    }

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

    fun updateEventInMatch(matchId: String, eventId: String, updatedEvent: MatchEvent, currentEvents: List<MatchEvent>, onResult: ((Result<Unit>) -> Unit)? = null) {
        val updatedList = currentEvents.map { if (it.id == eventId) updatedEvent else it }
        viewModelScope.launch {
            val result = repository.updateMatch(matchId, mapOf("events" to updatedList))
            onResult?.invoke(result)
        }
    }

    fun deleteEventFromMatch(matchId: String, eventId: String, currentEvents: List<MatchEvent>, onResult: ((Result<Unit>) -> Unit)? = null) {
        val updatedList = currentEvents.filter { it.id != eventId }
        viewModelScope.launch {
            val result = repository.updateMatch(matchId, mapOf("events" to updatedList))
            onResult?.invoke(result)
        }
    }
}
