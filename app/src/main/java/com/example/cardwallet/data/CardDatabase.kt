package com.example.cardwallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cardwallet.data.dao.CardDao
import com.example.cardwallet.data.dao.CategoryDao
import com.example.cardwallet.data.model.CardEntity
import com.example.cardwallet.data.model.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CardEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_wallet.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.categoryDao(), database.cardDao())
                    }
                }
            }

            suspend fun populateInitialData(categoryDao: CategoryDao, cardDao: CardDao) {
                // Pre-populate standard categories
                val categories = listOf(
                    CategoryEntity(name = "Loyalty", colorHex = "#1E88E5", iconName = "loyalty"),
                    CategoryEntity(name = "Travel", colorHex = "#43A047", iconName = "flight"),
                    CategoryEntity(name = "Membership", colorHex = "#8E24AA", iconName = "badge"),
                    CategoryEntity(name = "Identity", colorHex = "#E53935", iconName = "person"),
                    CategoryEntity(name = "Payment", colorHex = "#FB8C00", iconName = "credit_card"),
                    CategoryEntity(name = "Coupon", colorHex = "#00ACC1", iconName = "local_offer")
                )
                categoryDao.insertAll(categories)

                // Pre-populate sample cards
                val sampleCards = listOf(
                    CardEntity(
                        title = "SkyWings Frequent Flyer",
                        issuer = "SkyWings Airlines",
                        cardNumber = "SW-8892-0194",
                        cardholderName = "Alex Morgan",
                        barcodeValue = "SW88920194",
                        barcodeFormat = "AZTEC",
                        category = "Travel",
                        colorHex = "#0D47A1",
                        secondaryColorHex = "#1976D2",
                        notes = "Priority Boarding Zone 1 • Star Alliance Gold",
                        expiryDate = "12/28",
                        isFavorite = true
                    ),
                    CardEntity(
                        title = "Emerald Coffee Club",
                        issuer = "Emerald Roast Co.",
                        cardNumber = "9842 1045 7731",
                        cardholderName = "Alex Morgan",
                        barcodeValue = "984210457731",
                        barcodeFormat = "QR_CODE",
                        category = "Loyalty",
                        colorHex = "#1B5E20",
                        secondaryColorHex = "#2E7D32",
                        notes = "Free beverage every 10 stars • 24 stars accumulated",
                        expiryDate = "No Expiry",
                        isFavorite = true
                    ),
                    CardEntity(
                        title = "Apex Fitness Pass",
                        issuer = "Apex Health & Fitness",
                        cardNumber = "AF-550183",
                        cardholderName = "Alex Morgan",
                        barcodeValue = "AF550183",
                        barcodeFormat = "CODE_128",
                        category = "Membership",
                        colorHex = "#4A148C",
                        secondaryColorHex = "#7B1FA2",
                        notes = "24/7 Access to all regional facilities",
                        expiryDate = "09/27",
                        isFavorite = false
                    ),
                    CardEntity(
                        title = "Metro Transit Express",
                        issuer = "City Metro Authority",
                        cardNumber = "7730 4912 6601",
                        cardholderName = "Alex Morgan",
                        barcodeValue = "773049126601",
                        barcodeFormat = "PDF_417",
                        category = "Travel",
                        colorHex = "#E65100",
                        secondaryColorHex = "#F57C00",
                        notes = "Monthly Unlimited Pass (Subway & Bus)",
                        expiryDate = "10/26",
                        isFavorite = false
                    )
                )
                cardDao.insertAll(sampleCards)
            }
        }
    }
}
