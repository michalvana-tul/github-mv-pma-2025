package com.example.sportshub.models

data class Match(
    val id: String = "",
    val sportId: String = "",
    val sportName: String = "",
    val sportType: SportType = SportType.TEAM,

    // Týmové informace
    val homeTeam: String = "",
    val awayTeam: String = "",
    val homeTeamColor: String = "#6200EE",
    val awayTeamColor: String = "#03DAC5",

    // Skóre
    val homeScore: Int = 0,
    val awayScore: Int = 0,

    // Pro individuální aktivity
    val duration: Int = 0, // v minutách
    val notes: String = "",

    // Stav
    val isLive: Boolean = false,
    val isFinished: Boolean = false,

    // Timeline
    val events: List<MatchEvent> = emptyList(),

    // Statistiky
    val possession: Map<String, Int>? = null,
    val shots: Map<String, Int>? = null,

    // Metadata
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long = System.currentTimeMillis() + (90 * 60 * 1000),
    val imageUrl: String? = null
)
