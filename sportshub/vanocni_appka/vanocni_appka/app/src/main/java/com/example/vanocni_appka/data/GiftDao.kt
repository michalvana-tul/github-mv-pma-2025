package com.example.vanocni_appka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts ORDER BY id DESC")
    fun getAllGifts(): Flow<List<Gift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGift(gift: Gift)

    @Update
    suspend fun updateGift(gift: Gift)

    @Delete
    suspend fun deleteGift(gift: Gift)

    @Query("SELECT * FROM gifts WHERE id = :id")
    suspend fun getGiftById(id: Int): Gift?
}
