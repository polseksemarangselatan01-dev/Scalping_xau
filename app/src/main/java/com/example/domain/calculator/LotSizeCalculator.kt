package com.example.domain.calculator

import com.example.data.model.CalculatorResult
import kotlin.math.abs
import kotlin.math.max

object LotSizeCalculator {

    fun calculate(
        balance: Double,
        equity: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLossPrice: Double,
        takeProfit1: Double = 0.0,
        takeProfit2: Double = 0.0,
        takeProfit3: Double = 0.0,
        contractSize: Double = 100.0, // Standard 100 oz for XAU/USD
        currency: String = "USD"
    ): CalculatorResult {
        val safeBalance = max(balance, 10.0)
        val safeRiskPercent = riskPercent.coerceIn(0.1, 50.0)
        val riskAmount = safeBalance * (safeRiskPercent / 100.0)

        val priceDifference = abs(entryPrice - stopLossPrice)
        val pipDifference = if (priceDifference > 0.01) priceDifference else 2.0 // Minimum 2 dollars default

        // Lot Size formula: Risk ($) / (Price Distance ($) * Contract Size)
        val rawLot = riskAmount / (pipDifference * contractSize)
        // Normalize lot to broker increments (0.01 lot minimum)
        val recommendedLot = max(((rawLot * 100).toLong() / 100.0), 0.01)

        val estimatedLoss = recommendedLot * pipDifference * contractSize

        val tp1Dist = if (takeProfit1 > 0) abs(takeProfit1 - entryPrice) else pipDifference * 1.5
        val tp2Dist = if (takeProfit2 > 0) abs(takeProfit2 - entryPrice) else pipDifference * 2.5
        val tp3Dist = if (takeProfit3 > 0) abs(takeProfit3 - entryPrice) else pipDifference * 4.0

        val profitTp1 = recommendedLot * tp1Dist * contractSize
        val profitTp2 = recommendedLot * tp2Dist * contractSize
        val profitTp3 = recommendedLot * tp3Dist * contractSize

        return CalculatorResult(
            balance = safeBalance,
            equity = equity,
            riskPercent = safeRiskPercent,
            riskAmount = (riskAmount * 100).toLong() / 100.0,
            entryPrice = entryPrice,
            stopLossPrice = stopLossPrice,
            pipDifference = (pipDifference * 100).toLong() / 100.0,
            recommendedLot = recommendedLot,
            estimatedLoss = (estimatedLoss * 100).toLong() / 100.0,
            estimatedProfitTp1 = (profitTp1 * 100).toLong() / 100.0,
            estimatedProfitTp2 = (profitTp2 * 100).toLong() / 100.0,
            estimatedProfitTp3 = (profitTp3 * 100).toLong() / 100.0,
            contractSize = contractSize,
            currency = currency
        )
    }
}
