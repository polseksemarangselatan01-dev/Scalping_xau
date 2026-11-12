package com.example.domain.analyzer

import com.example.data.model.Candle
import com.example.data.model.MultiTimeframeSummary
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.data.model.TradingSignal
import kotlin.math.abs

object SignalGenerator {

    fun generateSignal(
        timeframe: Timeframe,
        candles: List<Candle>,
        mtfSummary: MultiTimeframeSummary? = null
    ): TradingSignal? {
        // Guard: Do not generate signals if market data is incomplete
        if (candles.size < 20) {
            return null
        }

        val analysis = MultiTimeframeEngine.analyzeSingleTimeframe(timeframe, candles)
        val currentPrice = analysis.currentPrice
        val atr = if (analysis.atr14 > 0.5) analysis.atr14 else 2.00 // Pips scaling

        val type: SignalType = if (mtfSummary != null && mtfSummary.confluenceScore >= 3) {
            mtfSummary.overallBias
        } else {
            analysis.summaryBias
        }

        // Validity duration: M1 = 10 mins, M5 = 25 mins, M15 = 45 mins, H1 = 120 mins
        val validityMinutes = when (timeframe) {
            Timeframe.M1 -> 10
            Timeframe.M5 -> 25
            Timeframe.M15 -> 45
            Timeframe.H1 -> 120
        }
        val validUntil = System.currentTimeMillis() + (validityMinutes * 60 * 1000L)

        val entryPrice = currentPrice
        val technicalReasons = mutableListOf<String>()

        val stopLoss: Double
        val tp1: Double
        val tp2: Double
        val tp3: Double
        val invalidationCondition: String
        val rrr: Double

        when (type) {
            SignalType.STRONG_BUY, SignalType.BUY -> {
                // SL below support / ATR buffer
                val slDistance = (atr * 1.2).coerceAtLeast(1.80)
                stopLoss = ((entryPrice - slDistance) * 100).toLong() / 100.0

                val risk = entryPrice - stopLoss
                tp1 = ((entryPrice + (risk * 1.5)) * 100).toLong() / 100.0
                tp2 = ((entryPrice + (risk * 2.5)) * 100).toLong() / 100.0
                tp3 = ((entryPrice + (risk * 4.0)) * 100).toLong() / 100.0
                rrr = 2.5

                technicalReasons.add("EMA Stack: EMA 9 (${analysis.ema9}) > EMA 21 (${analysis.ema21}) bullish crossover")
                technicalReasons.add("RSI 14 Momentum at ${analysis.rsi14} in healthy upside expansion zone")
                technicalReasons.add("SMC Order Flow: ${analysis.smcStructure}")
                if (analysis.liquiditySweep.contains("Swept", ignoreCase = true)) {
                    technicalReasons.add(analysis.liquiditySweep)
                }
                technicalReasons.add("VWAP Support: Price trading cleanly above institutional VWAP (${analysis.vwap})")

                invalidationCondition = "Batal otomatis jika candle $timeframe close di bawah Stop Loss ($stopLoss) atau terjadi Bearish CHoCH mendadak."
            }

            SignalType.STRONG_SELL, SignalType.SELL -> {
                val slDistance = (atr * 1.2).coerceAtLeast(1.80)
                stopLoss = ((entryPrice + slDistance) * 100).toLong() / 100.0

                val risk = stopLoss - entryPrice
                tp1 = ((entryPrice - (risk * 1.5)) * 100).toLong() / 100.0
                tp2 = ((entryPrice - (risk * 2.5)) * 100).toLong() / 100.0
                tp3 = ((entryPrice - (risk * 4.0)) * 100).toLong() / 100.0
                rrr = 2.5

                technicalReasons.add("EMA Stack: EMA 9 (${analysis.ema9}) < EMA 21 (${analysis.ema21}) bearish rejection")
                technicalReasons.add("RSI 14 Momentum at ${analysis.rsi14} breaking down below equilibrium")
                technicalReasons.add("SMC Structure: ${analysis.smcStructure}")
                if (analysis.liquiditySweep.contains("Swept", ignoreCase = true)) {
                    technicalReasons.add(analysis.liquiditySweep)
                }
                technicalReasons.add("VWAP Resistance: Price rejected by institutional VWAP (${analysis.vwap})")

                invalidationCondition = "Batal otomatis jika candle $timeframe close di atas Stop Loss ($stopLoss) atau terjadi Bullish Liquidity Absorption."
            }

            SignalType.WAIT -> {
                stopLoss = entryPrice - 2.50
                tp1 = entryPrice + 2.50
                tp2 = entryPrice + 5.00
                tp3 = entryPrice + 8.00
                rrr = 1.0
                technicalReasons.add("Pasar berkonsolidasi di dalam range sempit (${analysis.support1} - ${analysis.resistance1})")
                technicalReasons.add("Indikator EMA & RSI bertentangan tanpa konfirmasi volume")
                technicalReasons.add("Belum ada liquidity sweep yang valid untuk trigger scalping")
                invalidationCondition = "Tunggu breakout jelas di atas ${analysis.resistance1} atau di bawah ${analysis.support1} sebelum entry."
            }
        }

        val confidence = if (type == SignalType.WAIT) 50 else analysis.confidence

        return TradingSignal(
            pair = "XAU/USD",
            type = type,
            timeframe = timeframe,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit1 = tp1,
            takeProfit2 = tp2,
            takeProfit3 = tp3,
            riskRewardRatio = rrr,
            confidenceScore = confidence,
            technicalReasons = technicalReasons,
            validUntilTimestamp = validUntil,
            invalidationCondition = invalidationCondition,
            marketCondition = if (type == SignalType.WAIT) "Sideways / Choppy Market" else "High Probability Confluence Scalp"
        )
    }
}
