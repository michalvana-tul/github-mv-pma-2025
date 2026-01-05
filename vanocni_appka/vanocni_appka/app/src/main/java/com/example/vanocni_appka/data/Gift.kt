package com.example.vanocni_appka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gifts")
data class Gift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val person: String,
    val imageUri: String? = null,
    val isBought: Boolean = false
)
