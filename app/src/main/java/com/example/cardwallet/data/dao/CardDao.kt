package com.example.cardwallet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cardwallet.data.model.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE isArchived = 0 ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllActiveCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isArchived = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isArchived = 0 AND category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getCardsByCategory(category: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR issuer LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') ORDER BY isFavorite DESC, updatedAt DESC")
    fun searchCards(query: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("UPDATE cards SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, timestamp: Long): Int

    @Query("UPDATE cards SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE isArchived = 0")
    suspend fun getActiveCardCount(): Int

    @Query("DELETE FROM cards WHERE isArchived = 1")
    suspend fun emptyTrash(): Int
}
