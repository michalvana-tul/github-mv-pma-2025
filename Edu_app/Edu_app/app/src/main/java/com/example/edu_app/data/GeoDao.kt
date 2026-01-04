package com.example.edu_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoDao {
    @Query("SELECT * FROM users WHERE username = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM countries")
    suspend fun getAllCountries(): List<CountryEntity>

    @Query("SELECT * FROM countries ORDER BY countryName ASC")
    fun getAllCountriesFlow(): Flow<List<CountryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)

    @Update
    suspend fun updateCountry(country: CountryEntity)

    @Delete
    suspend fun deleteCountry(country: CountryEntity)

    @Insert
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Insert
    suspend fun insertResult(result: GameResultEntity)

    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getResultsForUser(userId: Int): Flow<List<GameResultEntity>>
}
