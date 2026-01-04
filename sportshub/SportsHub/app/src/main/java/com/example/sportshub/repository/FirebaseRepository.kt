package com.example.sportshub.repository

import com.example.sportshub.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
            val docRef = sportsRef.document()
            docRef.set(sport).await()
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
        val listener = matchesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val now = System.currentTimeMillis()
            
            val allMatches = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Match::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            val filteredMatches = when (filter) {
                MatchFilter.UPCOMING -> allMatches.filter { 
                    if (it.sportType == SportType.INDIVIDUAL) {
                        it.duration == 0
                    } else {
                        !it.isFinished && it.endTimestamp > now 
                    }
                }
                
                MatchFilter.FINISHED -> allMatches.filter { 
                    if (it.sportType == SportType.INDIVIDUAL) {
                        it.duration > 0
                    } else {
                        it.isFinished || it.endTimestamp <= now 
                    }
                }
                
                MatchFilter.LIVE -> allMatches.filter { 
                    it.sportType == SportType.TEAM && 
                    (it.isLive || (it.timestamp <= now && it.endTimestamp > now)) && !it.isFinished 
                }
                
                MatchFilter.ALL -> allMatches
                else -> allMatches // Failsafe
            }

            trySend(filteredMatches.sortedByDescending { it.timestamp })
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMatch(match: Match): Result<String> {
        return try {
            val docRef = matchesRef.document()
            val matchMap = hashMapOf(
                "sportId" to match.sportId,
                "sportName" to match.sportName,
                "sportType" to match.sportType.name,
                "homeTeam" to match.homeTeam,
                "awayTeam" to match.awayTeam,
                "homeTeamColor" to match.homeTeamColor,
                "awayTeamColor" to match.awayTeamColor,
                "homeScore" to match.homeScore,
                "awayScore" to match.awayScore,
                "duration" to match.duration,
                "notes" to match.notes,
                "isLive" to match.isLive,
                "isFinished" to match.isFinished,
                "date" to match.date,
                "timestamp" to match.timestamp,
                "endTimestamp" to match.endTimestamp,
                "events" to emptyList<Any>()
            )

            docRef.set(matchMap).await()
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

    suspend fun deleteAllMatches(): Result<Unit> {
        return try {
            val snapshot = matchesRef.get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finishMatch(matchId: String, match: Match): Result<Unit> {
        val totalScore = match.homeScore + match.awayScore
        val possession = generatePossession(match.homeScore, match.awayScore)
        val shots = generateShots(match.homeScore, match.awayScore, totalScore)

        val updates = mapOf(
            "isLive" to false,
            "isFinished" to true,
            "endTimestamp" to System.currentTimeMillis(),
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
    ALL, UPCOMING, LIVE, FINISHED
}
