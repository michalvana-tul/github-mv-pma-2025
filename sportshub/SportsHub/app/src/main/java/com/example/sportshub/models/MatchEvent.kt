package com.example.sportshub.models

data class MatchEvent(
    val minute: Int = 0,
    val type: EventType = EventType.GOAL,
    val team: String = "", // "home" nebo "away"
    val playerName: String = "",
    val description: String = ""
)

enum class EventType {
    GOAL,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    OTHER
}
