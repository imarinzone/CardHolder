package com.example.cardwallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val issuer: String = "",
    val cardNumber: String = "",
    val cardholderName: String = "",
    val barcodeValue: String = "",
    val barcodeFormat: String = "QR_CODE", // QR_CODE, CODE_128, EAN_13, AZTEC, PDF_417, CODE_39
    val category: String = "Loyalty",
    val colorHex: String = "#1E88E5",
    val secondaryColorHex: String = "#1565C0",
    val notes: String = "",
    val expiryDate: String = "",
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
