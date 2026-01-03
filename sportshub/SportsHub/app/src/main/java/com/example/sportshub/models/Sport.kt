package com.example.sportshub.models

data class Sport(
    val id: String = "",
    val name: String = "",
    val type: SportType = SportType.TEAM,
    val icon: String = "", // URL nebo emoji
    val createdAt: Long = System.currentTimeMillis()
)