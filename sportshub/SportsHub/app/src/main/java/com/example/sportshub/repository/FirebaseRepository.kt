package com.example.sportshub.repository

import com.example.sportshub.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sportsRef = db.collection("sports")
    private val matchesRef = db.collection("matches")

    // ===== SPORTS =====
    fun getSportsFlow(): Flow<List<Sport>> = callbackFlow {
        val listener = sportsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val sports = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Sport::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(sports)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addSport(sport: Sport): Result<String> {
        return try {
            val docRef = sportsRef.add(sport).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSport(sportId: String): Result<Unit> {
        return try {
            sportsRef.document(sportId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== MATCHES =====
    fun getMatchesFlow(filter: MatchFilter = MatchFilter.ALL): Flow<List<Match>> = callbackFlow {
        var query: Query = matchesRef.orderBy("timestamp", Query.Direction.DESCENDING)

        query = when (filter) {
            MatchFilter.UPCOMING -> query.whereEqualTo("isFinished", false)
                .whereEqualTo("isLive", false)
            MatchFilter.LIVE -> query.whereEqualTo("isLive", true)
            MatchFilter.FINISHED -> query.whereEqualTo("isFinished", true)
            MatchFilter.FAVORITES -> query.whereEqualTo("isFavorite", true)
            MatchFilter.ALL -> query
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val matches = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Match::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(matches)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMatch(match: Match): Result<String> {
        return try {
            val docRef = matchesRef.add(match).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMatch(matchId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            matchesRef.document(matchId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMatch(matchId: String): Result<Unit> {
        return try {
            matchesRef.document(matchId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(matchId: String, isFavorite: Boolean): Result<Unit> {
        return updateMatch(matchId, mapOf("isFavorite" to isFavorite))
    }

    suspend fun finishMatch(matchId: String, match: Match): Result<Unit> {
        // Generování náhodných statistik
        val totalScore = match.homeScore + match.awayScore
        val possession = generatePossession(match.homeScore, match.awayScore)
        val shots = generateShots(match.homeScore, match.awayScore, totalScore)

        val updates = mapOf(
            "isLive" to false,
            "isFinished" to true,
            "possession" to possession,
            "shots" to shots
        )
        return updateMatch(matchId, updates)
    }

    private fun generatePossession(homeScore: Int, awayScore: Int): Map<String, Int> {
        val base = if (homeScore > awayScore) {
            55 + (homeScore - awayScore) * 5
        } else if (awayScore > homeScore) {
            45 - (awayScore - homeScore) * 5
        } else {
            50
        }
        val homePoss = base.coerceIn(35, 65)
        return mapOf("first" to homePoss, "second" to (100 - homePoss))
    }

    private fun generateShots(homeScore: Int, awayScore: Int, totalScore: Int): Map<String, Int> {
        val baseShots = (totalScore * 3) + (5..10).random()
        val homeShots = if (homeScore > awayScore) {
            (baseShots * 0.6).toInt() + (0..3).random()
        } else {
            (baseShots * 0.4).toInt() + (0..3).random()
        }
        val awayShots = baseShots - homeShots
        return mapOf("first" to homeShots, "second" to awayShots)
    }
}

enum class MatchFilter {
    ALL, UPCOMING, LIVE, FINISHED, FAVORITES
}
