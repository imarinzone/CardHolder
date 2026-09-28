package com.example.cardwallet.data.repository

import com.example.cardwallet.data.dao.CardDao
import com.example.cardwallet.data.dao.CategoryDao
import com.example.cardwallet.data.model.CardEntity
import com.example.cardwallet.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CardDao,
    private val categoryDao: CategoryDao
) {
    val activeCards: Flow<List<CardEntity>> = cardDao.getAllActiveCards()
    val favoriteCards: Flow<List<CardEntity>> = cardDao.getFavoriteCards()
    val archivedCards: Flow<List<CardEntity>> = cardDao.getArchivedCards()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCardsByCategory(category: String): Flow<List<CardEntity>> {
        return cardDao.getCardsByCategory(category)
    }

    fun searchCards(query: String): Flow<List<CardEntity>> {
        return cardDao.searchCards(query)
    }

    suspend fun getCardById(id: Long): CardEntity? {
        return cardDao.getCardById(id)
    }

    suspend fun insertCard(card: CardEntity): Long {
        return cardDao.insertCard(card)
    }

    suspend fun updateCard(card: CardEntity) {
        cardDao.updateCard(card)
    }

    suspend fun deleteCard(card: CardEntity) {
        cardDao.deleteCard(card)
    }

    suspend fun setArchived(id: Long, isArchived: Boolean) {
        cardDao.setArchived(id, isArchived, System.currentTimeMillis())
    }

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        cardDao.setFavorite(id, isFavorite, System.currentTimeMillis())
    }

    suspend fun emptyTrash() {
        cardDao.emptyTrash()
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
}
