package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Timeframe(val label: String, val minutes: Int, val description: String) {
    M1("M1", 1, "Scalp Entry"),
    M5("M5", 5, "Confirmation"),
    M15("M15", 15, "Intraday Trend"),
    H1("H1", 60, "Macro Direction")
}

enum class SignalType(val title: String) {
    STRONG_BUY("STRONG BUY"),
    BUY("BUY"),
    WAIT("WAIT / NEUTRAL"),
    SELL("SELL"),
    STRONG_SELL("STRONG SELL")
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class TickerPrice(
    val symbol: String = "XAU/USD",
    val bid: Double,
    val ask: Double,
    val spread: Double,
    val change: Double,
    val changePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val connectionState: ConnectionState = ConnectionState.DEMO
)

enum class ConnectionState {
    LIVE,
    OFFLINE,
    DEMO
}

data class TradingSignal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pair: String = "XAU/USD",
    val type: SignalType,
    val timeframe: Timeframe,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val riskRewardRatio: Double,
    val confidenceScore: Int,
    val technicalReasons: List<String>,
    val validUntilTimestamp: Long,
    val invalidationCondition: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val marketCondition: String = "High Confluence Scalp"
)

data class TechnicalAnalysisResult(
    val timeframe: Timeframe,
    val currentPrice: Double,
    val ema9: Double,
    val ema21: Double,
    val ema50: Double,
    val ema200: Double,
    val rsi14: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val bbUpper: Double,
    val bbMiddle: Double,
    val bbLower: Double,
    val atr14: Double,
    val vwap: Double,
    val support1: Double,
    val support2: Double,
    val resistance1: Double,
    val resistance2: Double,
    val smcStructure: String,
    val liquiditySweep: String,
    val orderBlockStatus: String,
    val summaryBias: SignalType,
    val confidence: Int
)

data class MultiTimeframeSummary(
    val m1: TechnicalAnalysisResult,
    val m5: TechnicalAnalysisResult,
    val m15: TechnicalAnalysisResult,
    val h1: TechnicalAnalysisResult,
    val overallBias: SignalType,
    val confluenceScore: Int, // 1 to 4
    val alignmentPercent: Int, // e.g. 75% or 100%
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalculatorResult(
    val balance: Double,
    val equity: Double,
    val riskPercent: Double,
    val riskAmount: Double,
    val entryPrice: Double,
    val stopLossPrice: Double,
    val pipDifference: Double,
    val recommendedLot: Double,
    val estimatedLoss: Double,
    val estimatedProfitTp1: Double,
    val estimatedProfitTp2: Double,
    val estimatedProfitTp3: Double,
    val contractSize: Double = 100.0,
    val currency: String = "USD"
)

@Entity(tableName = "trade_journal")
data class TradeJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pair: String = "XAU/USD",
    val signalType: String,
    val timeframe: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double? = null,
    val lotSize: Double = 0.1,
    val status: String = "OPEN", // OPEN, WIN, LOSS, CANCELLED
    val pnl: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)
