package com.example.domain.analyzer

import com.example.data.model.Candle
import com.example.data.model.MultiTimeframeSummary
import com.example.data.model.SignalType
import com.example.data.model.TechnicalAnalysisResult
import com.example.data.model.Timeframe

object MultiTimeframeEngine {

    fun analyzeSingleTimeframe(timeframe: Timeframe, candles: List<Candle>): TechnicalAnalysisResult {
        if (candles.isEmpty()) {
            return TechnicalAnalysisResult(
                timeframe = timeframe,
                currentPrice = 0.0,
                ema9 = 0.0,
                ema21 = 0.0,
                ema50 = 0.0,
                ema200 = 0.0,
                rsi14 = 50.0,
                macdLine = 0.0,
                macdSignal = 0.0,
                macdHistogram = 0.0,
                bbUpper = 0.0,
                bbMiddle = 0.0,
                bbLower = 0.0,
                atr14 = 1.5,
                vwap = 0.0,
                support1 = 0.0,
                support2 = 0.0,
                resistance1 = 0.0,
                resistance2 = 0.0,
                smcStructure = "Insufficient Data",
                liquiditySweep = "None",
                orderBlockStatus = "None",
                summaryBias = SignalType.WAIT,
                confidence = 50
            )
        }

        val currentPrice = candles.last().close
        val ema9 = TechnicalIndicators.calculateEma(candles, 9)
        val ema21 = TechnicalIndicators.calculateEma(candles, 21)
        val ema50 = TechnicalIndicators.calculateEma(candles, 50)
        val ema200 = TechnicalIndicators.calculateEma(candles, 200)

        val rsi14 = TechnicalIndicators.calculateRsi(candles, 14)
        val (macdLine, macdSignal, macdHist) = TechnicalIndicators.calculateMacd(candles)
        val (bbUpper, bbMiddle, bbLower) = TechnicalIndicators.calculateBollingerBands(candles)
        val atr14 = TechnicalIndicators.calculateAtr(candles)
        val vwap = TechnicalIndicators.calculateVwap(candles)
        val (supports, resistances) = TechnicalIndicators.calculateSupportResistance(candles)

        val smc = SmartMoneyConcepts.analyze(candles)

        // Quantitative Scoring
        var bullPoints = 0
        var bearPoints = 0

        // 1. EMA Trend Stack
        if (ema9 > ema21) bullPoints += 2 else bearPoints += 2
        if (currentPrice > ema50) bullPoints += 1 else bearPoints += 1
        if (currentPrice > ema200) bullPoints += 2 else bearPoints += 2

        // 2. RSI Momentum
        if (rsi14 in 50.0..70.0) bullPoints += 2
        else if (rsi14 in 30.0..50.0) bearPoints += 2
        else if (rsi14 > 70.0) bearPoints += 1 // Extreme overbought pullback risk
        else if (rsi14 < 30.0) bullPoints += 1 // Extreme oversold bounce potential

        // 3. MACD
        if (macdHist > 0) bullPoints += 1 else bearPoints += 1

        // 4. VWAP
        if (currentPrice > vwap) bullPoints += 1 else bearPoints += 1

        // 5. SMC Confluence
        if (smc.isBullishBias) bullPoints += 3
        if (smc.isBearishBias) bearPoints += 3

        val totalPoints = bullPoints + bearPoints
        val bias: SignalType
        val confidence: Int

        val diff = bullPoints - bearPoints
        when {
            diff >= 6 -> {
                bias = SignalType.STRONG_BUY
                confidence = (80 + (diff * 2).coerceAtMost(18))
            }
            diff in 2..5 -> {
                bias = SignalType.BUY
                confidence = (68 + (diff * 2).coerceAtMost(10))
            }
            diff in -1..1 -> {
                bias = SignalType.WAIT
                confidence = 50
            }
            diff in -5..-2 -> {
                bias = SignalType.SELL
                confidence = (68 + ((-diff) * 2).coerceAtMost(10))
            }
            else -> {
                bias = SignalType.STRONG_SELL
                confidence = (80 + ((-diff) * 2).coerceAtMost(18))
            }
        }

        return TechnicalAnalysisResult(
            timeframe = timeframe,
            currentPrice = currentPrice,
            ema9 = ema9,
            ema21 = ema21,
            ema50 = ema50,
            ema200 = ema200,
            rsi14 = rsi14,
            macdLine = macdLine,
            macdSignal = macdSignal,
            macdHistogram = macdHist,
            bbUpper = bbUpper,
            bbMiddle = bbMiddle,
            bbLower = bbLower,
            atr14 = atr14,
            vwap = vwap,
            support1 = supports.first,
            support2 = supports.second,
            resistance1 = resistances.first,
            resistance2 = resistances.second,
            smcStructure = smc.structure,
            liquiditySweep = smc.liquiditySweep,
            orderBlockStatus = smc.orderBlockStatus,
            summaryBias = bias,
            confidence = confidence
        )
    }

    fun evaluateMultiTimeframe(
        m1Candles: List<Candle>,
        m5Candles: List<Candle>,
        m15Candles: List<Candle>,
        h1Candles: List<Candle>
    ): MultiTimeframeSummary {
        val m1 = analyzeSingleTimeframe(Timeframe.M1, m1Candles)
        val m5 = analyzeSingleTimeframe(Timeframe.M5, m5Candles)
        val m15 = analyzeSingleTimeframe(Timeframe.M15, m15Candles)
        val h1 = analyzeSingleTimeframe(Timeframe.H1, h1Candles)

        val biases = listOf(m1.summaryBias, m5.summaryBias, m15.summaryBias, h1.summaryBias)

        val buyCount = biases.count { it == SignalType.BUY || it == SignalType.STRONG_BUY }
        val sellCount = biases.count { it == SignalType.SELL || it == SignalType.STRONG_SELL }

        val overallBias: SignalType
        val confluenceScore: Int
        val recommendation: String

        when {
            buyCount == 4 -> {
                overallBias = SignalType.STRONG_BUY
                confluenceScore = 4
                recommendation = "Full Confluence BUY: M1 scalper, M5 confirmation, M15 trend, and H1 macro structure are aligned bullish."
            }
            buyCount == 3 -> {
                overallBias = SignalType.BUY
                confluenceScore = 3
                recommendation = "High Probability BUY: 3/4 Timeframes confirm bullish momentum. Wait for M1 retest of Order Block before execution."
            }
            sellCount == 4 -> {
                overallBias = SignalType.STRONG_SELL
                confluenceScore = 4
                recommendation = "Full Confluence SELL: All 4 timeframes breaking down with institutional supply pressure."
            }
            sellCount == 3 -> {
                overallBias = SignalType.SELL
                confluenceScore = 3
                recommendation = "High Probability SELL: 3/4 Timeframes confirm bearish breakdown. Monitor Stop Loss above H1 resistance."
            }
            else -> {
                overallBias = SignalType.WAIT
                confluenceScore = maxOf(buyCount, sellCount)
                recommendation = "Timeframe Divergence: Conflicting signals between scalping and macro trend. Stand aside to preserve capital."
            }
        }

        val alignmentPercent = (confluenceScore * 25)

        return MultiTimeframeSummary(
            m1 = m1,
            m5 = m5,
            m15 = m15,
            h1 = h1,
            overallBias = overallBias,
            confluenceScore = confluenceScore,
            alignmentPercent = alignmentPercent,
            recommendation = recommendation
        )
    }
}
