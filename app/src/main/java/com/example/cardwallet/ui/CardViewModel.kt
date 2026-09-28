package com.example.cardwallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardwallet.data.CardDatabase
import com.example.cardwallet.data.model.CardEntity
import com.example.cardwallet.data.model.CategoryEntity
import com.example.cardwallet.data.repository.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CardDatabase.getDatabase(application, viewModelScope)
    private val repository = CardRepository(database.cardDao(), database.categoryDao())

    val allActiveCards: StateFlow<List<CardEntity>> = repository.activeCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCards: StateFlow<List<CardEntity>> = repository.favoriteCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedCards: StateFlow<List<CardEntity>> = repository.archivedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null) // null = all
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    val filteredCards: StateFlow<List<CardEntity>> = combine(
        allActiveCards,
        searchQuery,
        selectedCategory,
        onlyFavorites
    ) { cards, query, category, favoritesOnly ->
        cards.filter { card ->
            val matchesQuery = query.isBlank() ||
                card.title.contains(query, ignoreCase = true) ||
                card.issuer.contains(query, ignoreCase = true) ||
                card.cardNumber.contains(query, ignoreCase = true) ||
                card.notes.contains(query, ignoreCase = true)

            val matchesCategory = category == null || card.category.equals(category, ignoreCase = true)
            val matchesFavorites = !favoritesOnly || card.isFavorite

            matchesQuery && matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setOnlyFavorites(favoritesOnly: Boolean) {
        _onlyFavorites.value = favoritesOnly
    }

    fun saveCard(card: CardEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            if (card.id == 0L) {
                repository.insertCard(card)
            } else {
                repository.updateCard(card)
            }
            onComplete()
        }
    }

    fun toggleFavorite(card: CardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setFavorite(card.id, !card.isFavorite)
        }
    }

    fun archiveCard(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setArchived(cardId, true)
        }
    }

    fun restoreCard(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setArchived(cardId, false)
        }
    }

    fun deleteCardPermanently(card: CardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCard(card)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.emptyTrash()
        }
    }

    fun addCategory(name: String, colorHex: String = "#1E88E5") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = "folder"
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch(Dispatchers.IO) {
            val starterCards = listOf(
                CardEntity(
                    title = "Boarding Pass - NYC Flight",
                    issuer = "AeroGlobal Airlines",
                    cardNumber = "AG-4492-X",
                    cardholderName = "Jordan Reed",
                    barcodeValue = "AG4492XNYC",
                    barcodeFormat = "AZTEC",
                    category = "Travel",
                    colorHex = "#0D47A1",
                    secondaryColorHex = "#1976D2",
                    notes = "Seat 12B • Gate B22 • Priority Boarding",
                    expiryDate = "10/26",
                    isFavorite = true
                ),
                CardEntity(
                    title = "Artisan Bakery VIP",
                    issuer = "Artisan Bread & Cafe",
                    cardNumber = "9901 8821 3411",
                    cardholderName = "Jordan Reed",
                    barcodeValue = "990188213411",
                    barcodeFormat = "QR_CODE",
                    category = "Loyalty",
                    colorHex = "#1B5E20",
                    secondaryColorHex = "#2E7D32",
                    notes = "Tier Gold • 120 points available",
                    expiryDate = "No Expiry",
                    isFavorite = true
                ),
                CardEntity(
                    title = "National Museum Pass",
                    issuer = "Heritage Society",
                    cardNumber = "MEM-78229",
                    cardholderName = "Jordan Reed",
                    barcodeValue = "MEM78229",
                    barcodeFormat = "CODE_128",
                    category = "Membership",
                    colorHex = "#4A148C",
                    secondaryColorHex = "#7B1FA2",
                    notes = "Family Access Pass",
                    expiryDate = "12/27",
                    isFavorite = false
                ),
                CardEntity(
                    title = "SuperMart Rewards",
                    issuer = "SuperMart Grocers",
                    cardNumber = "4001 2390 8821",
                    cardholderName = "Jordan Reed",
                    barcodeValue = "400123908821",
                    barcodeFormat = "EAN_13",
                    category = "Loyalty",
                    colorHex = "#E65100",
                    secondaryColorHex = "#F57C00",
                    notes = "Scan at self-checkout for instant discount",
                    expiryDate = "05/29",
                    isFavorite = false
                )
            )
            for (card in starterCards) {
                repository.insertCard(card)
            }
        }
    }
}
