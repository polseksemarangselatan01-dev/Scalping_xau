package com.example

import com.example.data.model.Candle
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.domain.analyzer.MultiTimeframeEngine
import com.example.domain.analyzer.SignalGenerator
import com.example.domain.analyzer.TechnicalIndicators
import com.example.domain.calculator.LotSizeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLotSizeCalculator_standardRisk() {
        val result = LotSizeCalculator.calculate(
            balance = 5000.0,
            equity = 5000.0,
            riskPercent = 1.0, // $50 risk
            entryPrice = 2680.0,
            stopLossPrice = 2677.50, // $2.50 distance
            contractSize = 100.0 // Standard gold oz per lot
        )

        // Risk Amount = $50.00
        assertEquals(50.0, result.riskAmount, 0.01)
        // Pip/price diff = 2.50
        assertEquals(2.50, result.pipDifference, 0.01)
        // Lot = 50 / (2.50 * 100) = 50 / 250 = 0.20
        assertEquals(0.20, result.recommendedLot, 0.01)
        // Estimated loss = 0.20 * 2.50 * 100 = $50.00
        assertEquals(50.0, result.estimatedLoss, 0.01)
    }

    @Test
    fun testTechnicalIndicators_EmaAndRsi() {
        val candles = mutableListOf<Candle>()
        var price = 2650.0
        for (i in 0 until 30) {
            price += 1.0
            candles.add(
                Candle(
                    timestamp = System.currentTimeMillis() + (i * 60000L),
                    open = price - 0.5,
                    high = price + 1.0,
                    low = price - 1.0,
                    close = price,
                    volume = 100.0
                )
            )
        }

        val ema9 = TechnicalIndicators.calculateEma(candles, 9)
        val rsi14 = TechnicalIndicators.calculateRsi(candles, 14)
        val atr = TechnicalIndicators.calculateAtr(candles, 14)
        val vwap = TechnicalIndicators.calculateVwap(candles)

        assertTrue("EMA9 should be calculated", ema9 > 2650.0)
        assertTrue("RSI should reflect rising prices (> 50)", rsi14 > 50.0)
        assertTrue("ATR should be positive", atr > 0.0)
        assertTrue("VWAP should be positive", vwap > 2600.0)
    }

    @Test
    fun testSignalGenerator_outputContract() {
        val candles = mutableListOf<Candle>()
        var price = 2680.0
        for (i in 0 until 35) {
            price += 0.5
            candles.add(
                Candle(
                    timestamp = System.currentTimeMillis() + (i * 60000L),
                    open = price - 0.2,
                    high = price + 0.8,
                    low = price - 0.3,
                    close = price,
                    volume = 150.0
                )
            )
        }

        val signal = SignalGenerator.generateSignal(Timeframe.M5, candles)
        assertNotNull("Signal must be generated for sufficient candle history", signal)
        signal?.let {
            assertEquals("XAU/USD", it.pair)
            assertTrue("Entry price must be valid", it.entryPrice > 2000.0)
            assertTrue("Confidence score should be 0..100", it.confidenceScore in 0..100)
            assertTrue("Technical reasons list cannot be empty", it.technicalReasons.isNotEmpty())
            assertTrue("Invalidation condition must exist", it.invalidationCondition.isNotBlank())
        }
    }
}
