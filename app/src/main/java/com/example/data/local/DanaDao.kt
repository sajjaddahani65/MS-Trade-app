package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DanaDao {

    // Trade positions
    @Query("SELECT * FROM trade_positions WHERE accountType = :accountType AND status = :status ORDER BY openTime DESC")
    fun getPositionsByStatus(accountType: String, status: String): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE status = 'OPEN'")
    fun getAllOpenPositions(): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE status = 'OPEN'")
    suspend fun getAllOpenPositionsDirect(): List<TradePositionEntity>

    @Query("SELECT * FROM trade_positions WHERE accountType = :accountType ORDER BY openTime DESC")
    fun getAllPositions(accountType: String): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE id = :id")
    suspend fun getPositionById(id: Long): TradePositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: TradePositionEntity): Long

    @Update
    suspend fun updatePosition(position: TradePositionEntity)

    @Delete
    suspend fun deletePosition(position: TradePositionEntity)

    // Account Balances
    @Query("SELECT * FROM account_balances WHERE accountType = :accountType")
    fun getAccount(accountType: String): Flow<AccountEntity?>

    @Query("SELECT * FROM account_balances WHERE accountType = :accountType")
    suspend fun getAccountDirect(accountType: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: AccountEntity)

    // Watchlist
    @Query("SELECT * FROM watchlist")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteWatchlist(symbol: String)

    // Transactions
    @Query("SELECT * FROM transactions WHERE accountType = :accountType ORDER BY timestamp DESC")
    fun getTransactions(accountType: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Daily Reward & Mining
    @Query("SELECT * FROM rewards_state WHERE id = 1")
    fun getRewardState(): Flow<DailyRewardEntity?>

    @Query("SELECT * FROM rewards_state WHERE id = 1")
    suspend fun getRewardStateDirect(): DailyRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReward(reward: DailyRewardEntity)

    // Crypto Portfolio Holdings
    @Query("SELECT * FROM crypto_holdings WHERE accountType = :accountType ORDER BY amount * avgBuyPrice DESC")
    fun getHoldings(accountType: String): Flow<List<CryptoHoldingEntity>>

    @Query("SELECT * FROM crypto_holdings WHERE accountType = :accountType")
    suspend fun getHoldingsDirect(accountType: String): List<CryptoHoldingEntity>

    @Query("SELECT * FROM crypto_holdings WHERE accountType = :accountType AND symbol = :symbol LIMIT 1")
    suspend fun getHoldingBySymbol(accountType: String, symbol: String): CryptoHoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHolding(holding: CryptoHoldingEntity): Long

    @Delete
    suspend fun deleteHolding(holding: CryptoHoldingEntity)

    @Query("DELETE FROM crypto_holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: Long)
}
