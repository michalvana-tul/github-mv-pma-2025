package com.example.edu_app.ui

import androidx.lifecycle.*
import com.example.edu_app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GeoRepository) : ViewModel() {

    private val _user = MutableLiveData<UserEntity>()
    val user: LiveData<UserEntity> = _user

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _gameState = MutableLiveData<Int>(0) // 0: Login, 1: Menu, 2: Playing, 3: Profile, 4: GameOver, 5: AddQuestion, 6: ManageCountries
    val gameState: LiveData<Int> = _gameState

    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> = _score

    private val _questionCount = MutableLiveData<Int>(0)
    val questionCount: LiveData<Int> = _questionCount

    private val _pastResults = MutableLiveData<List<GameResultEntity>>()
    val pastResults: LiveData<List<GameResultEntity>> = _pastResults

    val allCountries: LiveData<List<CountryEntity>> = repository.getAllCountriesFlow().asLiveData()

    data class Question(
        val country: String,
        val correctCity: String,
        val options: List<String>
    )

    fun login(username: String) {
        viewModelScope.launch {
            val loggedUser = repository.getOrCreateUser(username)
            _user.value = loggedUser
            _gameState.value = 1 // Go to Menu
            
            // Observe past results for profile
            repository.getUserResults(loggedUser.id).collectLatest {
                _pastResults.postValue(it)
            }
        }
    }

    fun startNewGame() {
        _score.value = 0
        _questionCount.value = 0
        _gameState.value = 2 // Start Playing
        loadNewQuestion()
    }

    fun loadNewQuestion() {
        val currentCount = _questionCount.value ?: 0
        if (currentCount >= 10) {
            finishGameSession()
            return
        }

        viewModelScope.launch {
            var countries = repository.getAllCountries()
            
            // Retry logic if DB is still populating
            var retries = 0
            while (countries.size < 4 && retries < 5) {
                delay(500)
                countries = repository.getAllCountries()
                retries++
            }

            if (countries.size < 4) {
                return@launch
            }

            val correct = countries.random()
            val wrongs = countries.filter { it.id != correct.id }
                .shuffled()
                .take(3)
                .map { it.capitalCity }

            val options = (wrongs + correct.capitalCity).shuffled()
            _currentQuestion.postValue(Question(correct.countryName, correct.capitalCity, options))
        }
    }

    fun submitAnswer(answer: String) {
        if (answer == _currentQuestion.value?.correctCity) {
            _score.value = (_score.value ?: 0) + 10
        }
        _questionCount.value = (_questionCount.value ?: 0) + 1
        loadNewQuestion()
    }

    private fun finishGameSession() {
        viewModelScope.launch {
            user.value?.let { user ->
                val finalScore = _score.value ?: 0
                repository.saveResult(user.id, finalScore)
                
                // Update User Profile Stats
                user.totalGamesPlayed += 1
                if (finalScore > user.highScore) {
                    user.highScore = finalScore
                }
                
                // Simple average calculation update
                user.averageScore = (user.averageScore * (user.totalGamesPlayed - 1) + finalScore) / user.totalGamesPlayed
                user.lastPlayedDate = System.currentTimeMillis()
                
                repository.updateUser(user)
                _user.postValue(user)
            }
            _gameState.postValue(4) // Game Over
        }
    }

    fun addCountry(name: String, capital: String, region: String) {
        viewModelScope.launch {
            repository.addCountry(CountryEntity(countryName = name, capitalCity = capital, region = region))
            _gameState.postValue(1) // Return to menu after adding
        }
    }

    fun updateCountry(country: CountryEntity) {
        viewModelScope.launch {
            repository.updateCountry(country)
        }
    }

    fun deleteCountry(country: CountryEntity) {
        viewModelScope.launch {
            repository.deleteCountry(country)
        }
    }

    fun goToMenu() {
        _gameState.value = 1
    }

    fun goToProfile() {
        _gameState.value = 3
    }

    fun goToAddQuestion() {
        _gameState.value = 5
    }

    fun goToManageCountries() {
        _gameState.value = 6
    }
}

class GameViewModelFactory(private val repository: GeoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
