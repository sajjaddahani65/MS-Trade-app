package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TradePositionEntity::class,
        AccountEntity::class,
        WatchlistEntity::class,
        TransactionEntity::class,
        DailyRewardEntity::class,
        CryptoHoldingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DanaDatabase : RoomDatabase() {
    abstract fun danaDao(): DanaDao

    companion object {
        @Volatile
        private var INSTANCE: DanaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DanaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DanaDatabase::class.java,
                    "btc_dana_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DanaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DanaDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.danaDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: DanaDao) {
                // Initialize Demo Account with $10,000.00
                dao.insertOrUpdateAccount(
                    AccountEntity(
                        accountType = "DEMO",
                        balance = 10000.00,
                        currency = "USD",
                        bdcCoins = 3200,
                        vipLevel = 2
                    )
                )

                // Initialize Real Account with $250.00 test deposit
                dao.insertOrUpdateAccount(
                    AccountEntity(
                        accountType = "REAL",
                        balance = 250.00,
                        currency = "USD",
                        bdcCoins = 500,
                        vipLevel = 1
                    )
                )

                // Watchlist initial items
                listOf("BTC/USD", "XAU/USD", "EUR/USD", "NAS100", "ETH/USD").forEach { sym ->
                    dao.insertWatchlist(WatchlistEntity(symbol = sym, isFavorite = true))
                }

                // Initial open demo position for realism
                val now = System.currentTimeMillis()
                dao.insertPosition(
                    TradePositionEntity(
                        accountType = "DEMO",
                        symbol = "BTC/USD",
                        direction = "BUY",
                        lotSize = 0.20,
                        openPrice = 88450.00,
                        currentPrice = 89120.00,
                        takeProfit = 92000.00,
                        stopLoss = 86000.00,
                        leverage = 100,
                        margin = 176.90,
                        openTime = now - 3600000 * 4,
                        profit = 134.00,
                        status = "OPEN"
                    )
                )

                dao.insertPosition(
                    TradePositionEntity(
                        accountType = "DEMO",
                        symbol = "XAU/USD",
                        direction = "BUY",
                        lotSize = 0.50,
                        openPrice = 2715.40,
                        currentPrice = 2728.80,
                        takeProfit = 2750.00,
                        stopLoss = 2690.00,
                        leverage = 100,
                        margin = 135.77,
                        openTime = now - 3600000 * 2,
                        profit = 67.00,
                        status = "OPEN"
                    )
                )

                // Initial closed trades for history stats
                dao.insertPosition(
                    TradePositionEntity(
                        accountType = "DEMO",
                        symbol = "ETH/USD",
                        direction = "BUY",
                        lotSize = 0.50,
                        openPrice = 3320.00,
                        currentPrice = 3440.00,
                        takeProfit = 3450.00,
                        stopLoss = 3200.00,
                        leverage = 50,
                        margin = 33.20,
                        openTime = now - 86400000 * 2,
                        closeTime = now - 86400000,
                        closePrice = 3440.00,
                        profit = 60.00,
                        status = "CLOSED",
                        closeReason = "TAKE_PROFIT"
                    )
                )

                dao.insertPosition(
                    TradePositionEntity(
                        accountType = "DEMO",
                        symbol = "EUR/USD",
                        direction = "SELL",
                        lotSize = 1.00,
                        openPrice = 1.0890,
                        currentPrice = 1.0850,
                        takeProfit = 1.0820,
                        stopLoss = 1.0930,
                        leverage = 200,
                        margin = 54.45,
                        openTime = now - 86400000 * 3,
                        closeTime = now - 86400000 * 2,
                        closePrice = 1.0850,
                        profit = 40.00,
                        status = "CLOSED",
                        closeReason = "MANUAL"
                    )
                )

                // Initial transaction history
                dao.insertTransaction(
                    TransactionEntity(
                        accountType = "DEMO",
                        type = "DEPOSIT",
                        amount = 10000.00,
                        timestamp = now - 86400000 * 5,
                        description = "Welcome Demo Trading Fund"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        accountType = "REAL",
                        type = "DEPOSIT",
                        amount = 250.00,
                        timestamp = now - 86400000 * 2,
                        description = "BNB Smart Chain (0x0EBC...b52D) Credited"
                    )
                )

                // Initial reward state
                dao.insertOrUpdateReward(
                    DailyRewardEntity(
                        id = 1,
                        streakDays = 3,
                        lastCheckInTimestamp = now - 86400000,
                        isMiningActive = true,
                        miningEndTime = now + 43200000,
                        miningHashrate = "145.8 MH/s",
                        couponsCount = 2
                    )
                )

                // Initial Crypto Portfolio Holdings
                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "DEMO",
                        symbol = "BTC",
                        name = "Bitcoin",
                        amount = 0.45,
                        avgBuyPrice = 84150.00,
                        colorHex = "#F0B90B"
                    )
                )

                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "DEMO",
                        symbol = "ETH",
                        name = "Ethereum",
                        amount = 3.20,
                        avgBuyPrice = 3120.00,
                        colorHex = "#627EEA"
                    )
                )

                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "DEMO",
                        symbol = "SOL",
                        name = "Solana",
                        amount = 24.0,
                        avgBuyPrice = 162.50,
                        colorHex = "#00FFA3"
                    )
                )

                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "DEMO",
                        symbol = "BNB",
                        name = "BNB Smart Chain",
                        amount = 6.5,
                        avgBuyPrice = 540.00,
                        colorHex = "#F3BA2F"
                    )
                )

                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "REAL",
                        symbol = "BTC",
                        name = "Bitcoin",
                        amount = 0.0085,
                        avgBuyPrice = 85200.00,
                        colorHex = "#F0B90B"
                    )
                )

                dao.insertOrUpdateHolding(
                    CryptoHoldingEntity(
                        accountType = "REAL",
                        symbol = "SOL",
                        name = "Solana",
                        amount = 1.25,
                        avgBuyPrice = 175.00,
                        colorHex = "#00FFA3"
                    )
                )
            }
        }
    }
}
