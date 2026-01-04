package com.example.edu_app.data

import kotlinx.coroutines.flow.Flow

class GeoRepository(private val geoDao: GeoDao) {
    suspend fun getOrCreateUser(username: String): UserEntity {
        val existing = geoDao.getUserByName(username)
        if (existing != null) return existing
        
        val newUser = UserEntity(username = username)
        val id = geoDao.insertUser(newUser)
        return newUser.copy(id = id.toInt())
    }

    suspend fun getAllCountries() = geoDao.getAllCountries()

    fun getAllCountriesFlow(): Flow<List<CountryEntity>> = geoDao.getAllCountriesFlow()

    suspend fun addCountry(country: CountryEntity) {
        geoDao.insertCountry(country)
    }

    suspend fun updateCountry(country: CountryEntity) {
        geoDao.updateCountry(country)
    }

    suspend fun deleteCountry(country: CountryEntity) {
        geoDao.deleteCountry(country)
    }

    suspend fun saveResult(userId: Int, score: Int) {
        geoDao.insertResult(GameResultEntity(userId = userId, score = score))
    }
    
    suspend fun updateUser(user: UserEntity) = geoDao.updateUser(user)

    fun getUserResults(userId: Int): Flow<List<GameResultEntity>> = geoDao.getResultsForUser(userId)
}
