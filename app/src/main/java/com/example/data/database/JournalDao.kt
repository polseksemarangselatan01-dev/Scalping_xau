package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradeJournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM trade_journal ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeJournalEntity>>

    @Query("SELECT * FROM trade_journal WHERE status = :status ORDER BY timestamp DESC")
    fun getTradesByStatus(status: String): Flow<List<TradeJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeJournalEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeJournalEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeJournalEntity)

    @Query("DELETE FROM trade_journal WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trade_journal")
    suspend fun clearAllTrades()
}
