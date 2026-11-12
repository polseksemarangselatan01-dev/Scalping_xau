package com.example.domain.analyzer

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class SmcAnalysis(
    val structure: String,
    val liquiditySweep: String,
    val orderBlockStatus: String,
    val isBullishBias: Boolean,
    val isBearishBias: Boolean,
    val reasons: List<String>
)

object SmartMoneyConcepts {

    fun analyze(candles: List<Candle>): SmcAnalysis {
        if (candles.size < 10) {
            return SmcAnalysis(
                structure = "Range Consolidation",
                liquiditySweep = "None Detected",
                orderBlockStatus = "Neutral Order Flow",
                isBullishBias = false,
                isBearishBias = false,
                reasons = listOf("Accumulating volume data")
            )
        }

        val lastCandle = candles.last()
        val prevCandle = candles[candles.size - 2]
        val recent10 = candles.takeLast(10)

        // Find swing high & swing low
        val swingHigh = recent10.dropLast(2).maxOf { it.high }
        val swingLow = recent10.dropLast(2).minOf { it.low }

        val reasons = mutableListOf<String>()

        // 1. Break of Structure (BOS) & Change of Character (CHoCH)
        var structure = "Range Consolidation"
        var isBullish = false
        var isBearish = false

        if (lastCandle.close > swingHigh) {
            structure = "BOS Bullish (Break of Structure)"
            isBullish = true
            reasons.add("Bullish BOS: Candle closed above previous swing high ($swingHigh)")
        } else if (lastCandle.close < swingLow) {
            structure = "BOS Bearish (Break of Structure)"
            isBearish = true
            reasons.add("Bearish BOS: Candle closed below previous swing low ($swingLow)")
        } else if (lastCandle.close > prevCandle.high && prevCandle.close < prevCandle.open) {
            structure = "CHoCH Bullish (Change of Character)"
            isBullish = true
            reasons.add("Bullish CHoCH: Momentum shift from lower low to higher close")
        } else if (lastCandle.close < prevCandle.low && prevCandle.close > prevCandle.open) {
            structure = "CHoCH Bearish (Change of Character)"
            isBearish = true
            reasons.add("Bearish CHoCH: Momentum shift from higher high to lower close")
        }

        // 2. Liquidity Sweep Detection (Wick pushed past swing point but closed inside)
        var liquiditySweep = "No Sweep"
        if (lastCandle.high > swingHigh && lastCandle.close < swingHigh) {
            liquiditySweep = "Buy-Side Liquidity Swept (High Rejection)"
            isBearish = true
            reasons.add("Liquidity Sweep: Trapped buyers above $swingHigh with fast rejection")
        } else if (lastCandle.low < swingLow && lastCandle.close > swingLow) {
            liquiditySweep = "Sell-Side Liquidity Swept (Low Rejection)"
            isBullish = true
            reasons.add("Liquidity Sweep: Hunted stop-losses below $swingLow with aggressive buyer absorption")
        }

        // 3. Order Block & Fair Value Gap (FVG)
        var orderBlock = "Unmitigated Neutral"
        val bodySize = abs(lastCandle.close - lastCandle.open)
        val upperWick = lastCandle.high - max(lastCandle.close, lastCandle.open)
        val lowerWick = min(lastCandle.close, lastCandle.open) - lastCandle.low

        if (lastCandle.close > lastCandle.open && bodySize > 1.8 && lowerWick > 0.8) {
            orderBlock = "Bullish Demand Order Block Formed"
            isBullish = true
            reasons.add("Institusional Demand Order Block at ${String.format("%.2f", lastCandle.low)}")
        } else if (lastCandle.close < lastCandle.open && bodySize > 1.8 && upperWick > 0.8) {
            orderBlock = "Bearish Supply Order Block Formed"
            isBearish = true
            reasons.add("Institusional Supply Order Block at ${String.format("%.2f", lastCandle.high)}")
        } else {
            orderBlock = "Fair Value Gap (FVG) Retest"
        }

        return SmcAnalysis(
            structure = structure,
            liquiditySweep = liquiditySweep,
            orderBlockStatus = orderBlock,
            isBullishBias = isBullish,
            isBearishBias = isBearish,
            reasons = if (reasons.isEmpty()) listOf("Price reacting inside balanced liquidity pool") else reasons
        )
    }
}
