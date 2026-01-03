package com.example.sportshub.models

data class Match(
    val id: String = "",
    val sportId: String = "",
    val sportName: String = "",
    val sportType: SportType = SportType.TEAM,

    // Týmové informace
    val homeTeam: String = "",
    val awayTeam: String = "",
    val homeTeamColor: String = "#6200EE", // Barva domácích
    val awayTeamColor: String = "#03DAC5", // Barva hostů

    // Skóre
    val homeScore: Int = 0,
    val awayScore: Int = 0,

    // Pro individuální aktivity
    val duration: Int = 0, // v minutách
    val notes: String = "",

    // Stav
    val isLive: Boolean = false,
    val isFinished: Boolean = false,
    val isFavorite: Boolean = false,

    // Timeline
    val events: List<MatchEvent> = emptyList(),

    // Statistiky (generované)
    val possession: Pair<Int, Int>? = null, // např. (60, 40)
    val shots: Pair<Int, Int>? = null,      // např. (12, 8)

    // Metadata
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)