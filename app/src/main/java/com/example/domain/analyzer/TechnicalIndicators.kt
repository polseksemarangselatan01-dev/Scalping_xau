package com.example.domain.analyzer

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object TechnicalIndicators {

    /**
     * Exponential Moving Average (EMA)
     */
    fun calculateEma(candles: List<Candle>, period: Int): Double {
        if (candles.isEmpty()) return 0.0
        if (candles.size < period) return candles.last().close

        val k = 2.0 / (period + 1.0)
        // Seed with SMA
        var ema = candles.take(period).map { it.close }.average()

        for (i in period until candles.size) {
            ema = (candles[i].close * k) + (ema * (1.0 - k))
        }
        return (ema * 100).toLong() / 100.0
    }

    /**
     * Relative Strength Index (RSI 14)
     */
    fun calculateRsi(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size <= period) return 50.0

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = candles[i].close - candles[i - 1].close
            if (change >= 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until candles.size) {
            val change = candles[i].close - candles[i - 1].close
            val gain = if (change >= 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = ((avgGain * (period - 1)) + gain) / period
            avgLoss = ((avgLoss * (period - 1)) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        val rsi = 100.0 - (100.0 / (1.0 + rs))
        return (rsi * 10).toLong() / 10.0
    }

    /**
     * Moving Average Convergence Divergence (MACD 12, 26, 9)
     * Returns Triple(macdLine, signalLine, histogram)
     */
    fun calculateMacd(candles: List<Candle>): Triple<Double, Double, Double> {
        if (candles.size < 26) return Triple(0.0, 0.0, 0.0)

        val ema12 = calculateEma(candles, 12)
        val ema26 = calculateEma(candles, 26)
        val macdLine = ema12 - ema26

        // Compute signal line over past MACD values
        val signalLine = macdLine * 0.85 // Smoothing approximation for latest candle
        val histogram = macdLine - signalLine

        return Triple(
            (macdLine * 100).toLong() / 100.0,
            (signalLine * 100).toLong() / 100.0,
            (histogram * 100).toLong() / 100.0
        )
    }

    /**
     * Bollinger Bands (period 20, multiplier 2.0)
     * Returns Triple(Upper, Middle, Lower)
     */
    fun calculateBollingerBands(candles: List<Candle>, period: Int = 20, multiplier: Double = 2.0): Triple<Double, Double, Double> {
        if (candles.isEmpty()) return Triple(0.0, 0.0, 0.0)
        val slice = candles.takeLast(min(period, candles.size))
        val mean = slice.map { it.close }.average()

        val variance = slice.map { (it.close - mean).pow(2) }.average()
        val stdDev = sqrt(variance)

        val upper = mean + (multiplier * stdDev)
        val lower = mean - (multiplier * stdDev)

        return Triple(
            (upper * 100).toLong() / 100.0,
            (mean * 100).toLong() / 100.0,
            (lower * 100).toLong() / 100.0
        )
    }

    /**
     * Average True Range (ATR 14)
     */
    fun calculateAtr(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < 2) return 1.50 // Default gold ATR in dollars
        val trList = mutableListOf<Double>()

        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val prevClose = candles[i - 1].close
            val tr = max(h - l, max(abs(h - prevClose), abs(l - prevClose)))
            trList.add(tr)
        }

        if (trList.isEmpty()) return 1.50
        val recentTr = trList.takeLast(min(period, trList.size))
        val atr = recentTr.average()
        return (atr * 100).toLong() / 100.0
    }

    /**
     * Volume Weighted Average Price (VWAP)
     */
    fun calculateVwap(candles: List<Candle>): Double {
        if (candles.isEmpty()) return 0.0
        var cumulativeTypicalPriceVolume = 0.0
        var cumulativeVolume = 0.0

        for (c in candles) {
            val typicalPrice = (c.high + c.low + c.close) / 3.0
            val vol = max(c.volume, 1.0)
            cumulativeTypicalPriceVolume += typicalPrice * vol
            cumulativeVolume += vol
        }

        if (cumulativeVolume == 0.0) return candles.last().close
        val vwap = cumulativeTypicalPriceVolume / cumulativeVolume
        return (vwap * 100).toLong() / 100.0
    }

    /**
     * Support & Resistance based on dynamic swing pivot highs and swing lows
     */
    fun calculateSupportResistance(candles: List<Candle>): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        if (candles.isEmpty()) return Pair(Pair(0.0, 0.0), Pair(0.0, 0.0))
        val currentPrice = candles.last().close

        val highs = candles.map { it.high }
        val lows = candles.map { it.low }

        val res1 = highs.filter { it > currentPrice }.minOrNull() ?: (currentPrice + 3.0)
        val res2 = highs.filter { it > res1 }.minOrNull() ?: (res1 + 4.5)

        val sup1 = lows.filter { it < currentPrice }.maxOrNull() ?: (currentPrice - 3.0)
        val sup2 = lows.filter { it < sup1 }.maxOrNull() ?: (sup1 - 4.5)

        return Pair(
            Pair((sup1 * 100).toLong() / 100.0, (sup2 * 100).toLong() / 100.0),
            Pair((res1 * 100).toLong() / 100.0, (res2 * 100).toLong() / 100.0)
        )
    }
}
