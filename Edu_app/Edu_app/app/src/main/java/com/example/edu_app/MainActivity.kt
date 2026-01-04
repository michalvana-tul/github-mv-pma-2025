package com.example.edu_app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edu_app.data.CountryEntity
import com.example.edu_app.data.GeoDatabase
import com.example.edu_app.data.GeoRepository
import com.example.edu_app.databinding.ActivityMainBinding
import com.example.edu_app.ui.CountriesAdapter
import com.example.edu_app.ui.GameViewModel
import com.example.edu_app.ui.GameViewModelFactory
import com.example.edu_app.ui.ResultsAdapter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var countriesAdapter: CountriesAdapter
    
    private var editingCountry: CountryEntity? = null
    
    private val viewModel: GameViewModel by viewModels {
        val database = GeoDatabase.getDatabase(this, lifecycleScope)
        GameViewModelFactory(GeoRepository(database.geoDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Login Screen
        binding.btnLogin.setOnClickListener {
            val name = binding.etUsername.text.toString().trim()
            if (name.isNotBlank()) {
                viewModel.login(name)
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        // Menu Screen
        binding.btnNewGame.setOnClickListener {
            viewModel.startNewGame()
        }
        binding.btnViewProfile.setOnClickListener {
            viewModel.goToProfile()
        }
        binding.btnGoToAddQuestion.setOnClickListener {
            editingCountry = null
            binding.tvAddQuestionTitle.text = "Add New Country"
            binding.etNewCountry.text?.clear()
            binding.etNewCapital.text?.clear()
            binding.etNewRegion.text?.clear()
            viewModel.goToAddQuestion()
        }
        binding.btnManageCountries.setOnClickListener {
            viewModel.goToManageCountries()
        }

        // Game Screen
        val optionButtons = listOf(binding.btnOpt1, binding.btnOpt2, binding.btnOpt3, binding.btnOpt4)
        optionButtons.forEach { button ->
            button.setOnClickListener {
                viewModel.submitAnswer(button.text.toString())
            }
        }

        // Profile Screen
        resultsAdapter = ResultsAdapter(emptyList())
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = resultsAdapter
        }
        binding.btnProfileBack.setOnClickListener {
            viewModel.goToMenu()
        }

        // Manage Countries Screen
        countriesAdapter = CountriesAdapter(emptyList(), 
            onEdit = { country ->
                editingCountry = country
                binding.tvAddQuestionTitle.text = "Edit Country"
                binding.etNewCountry.setText(country.countryName)
                binding.etNewCapital.setText(country.capitalCity)
                binding.etNewRegion.setText(country.region)
                viewModel.goToAddQuestion()
            },
            onDelete = { country ->
                viewModel.deleteCountry(country)
                Toast.makeText(this, "Deleted ${country.countryName}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCountries.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = countriesAdapter
        }
        binding.btnManageCountriesBack.setOnClickListener {
            viewModel.goToMenu()
        }

        // Game Over Screen
        binding.btnDone.setOnClickListener {
            viewModel.goToMenu()
        }

        // Add/Edit Question Screen
        binding.btnSaveCountry.setOnClickListener {
            val countryName = binding.etNewCountry.text.toString().trim()
            val capital = binding.etNewCapital.text.toString().trim()
            val region = binding.etNewRegion.text.toString().trim()

            if (countryName.isNotBlank() && capital.isNotBlank() && region.isNotBlank()) {
                if (editingCountry == null) {
                    viewModel.addCountry(countryName, capital, region)
                    Toast.makeText(this, "Country added!", Toast.LENGTH_SHORT).show()
                } else {
                    val updated = editingCountry!!.copy(
                        countryName = countryName,
                        capitalCity = capital,
                        region = region
                    )
                    viewModel.updateCountry(updated)
                    Toast.makeText(this, "Country updated!", Toast.LENGTH_SHORT).show()
                    viewModel.goToManageCountries()
                }
                binding.etNewCountry.text?.clear()
                binding.etNewCapital.text?.clear()
                binding.etNewRegion.text?.clear()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnAddQuestionBack.setOnClickListener {
            if (editingCountry != null) viewModel.goToManageCountries() else viewModel.goToMenu()
        }
    }

    private fun observeViewModel() {
        viewModel.gameState.observe(this) { state ->
            // Visibility logic for all screens
            binding.loginLayout.visibility = if (state == 0) View.VISIBLE else View.GONE
            binding.menuLayout.visibility = if (state == 1) View.VISIBLE else View.GONE
            binding.gameLayout.visibility = if (state == 2) View.VISIBLE else View.GONE
            binding.profileLayout.visibility = if (state == 3) View.VISIBLE else View.GONE
            binding.gameOverLayout.visibility = if (state == 4) View.VISIBLE else View.GONE
            binding.addQuestionLayout.visibility = if (state == 5) View.VISIBLE else View.GONE
            binding.manageCountriesLayout.visibility = if (state == 6) View.VISIBLE else View.GONE
            
            binding.root.requestLayout()
        }

        viewModel.user.observe(this) { user ->
            title = "GeoMaster - ${user.username}"
            binding.tvWelcome.text = "Welcome, ${user.username}!"
            binding.tvTotalGames.text = "Total Games Played: ${user.totalGamesPlayed}"
            binding.tvAvgScore.text = String.format(Locale.getDefault(), "Average Score: %.1f", user.averageScore)
            binding.tvHighScore.text = "High Score: ${user.highScore}"
        }

        viewModel.currentQuestion.observe(this) { question ->
            binding.tvQuestion.text = "What is the capital of ${question.country}?"
            binding.btnOpt1.text = question.options[0]
            binding.btnOpt2.text = question.options[1]
            binding.btnOpt3.text = question.options[2]
            binding.btnOpt4.text = question.options[3]
            binding.gameLayout.requestLayout()
            binding.tvQuestion.invalidate()
        }

        viewModel.score.observe(this) { score ->
            binding.tvScore.text = "Score: $score"
            binding.tvFinalScore.text = "Final Score: $score"
        }

        viewModel.questionCount.observe(this) { count ->
            binding.tvQuestionProgress.text = "Question: $count/10"
        }

        viewModel.pastResults.observe(this) { results ->
            resultsAdapter.updateData(results)
        }
        
        viewModel.allCountries.observe(this) { countries ->
            countriesAdapter.updateData(countries)
        }
    }
}
