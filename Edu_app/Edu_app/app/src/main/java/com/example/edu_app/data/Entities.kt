package com.example.edu_app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    var highScore: Int = 0,
    var totalGamesPlayed: Int = 0,
    var averageScore: Double = 0.0,
    var lastPlayedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val countryName: String,
    val capitalCity: String,
    val region: String
)

@Entity(
    tableName = "game_results",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
