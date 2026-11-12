package com.example.domain.analyzer

import android.util.Log
import com.example.data.datastore.AppSettings
import com.example.data.model.MultiTimeframeSummary
import com.example.data.model.TechnicalAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiAnalysisReport(
    val summaryThesis: String,
    val institutionalOrderFlow: String,
    val keyRiskFactors: List<String>,
    val executionChecklist: List<String>,
    val isCloudAiGenerated: Boolean
)

class GeminiTradingAdvisor {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun generateDeepAnalysis(
        analysis: TechnicalAnalysisResult,
        mtf: MultiTimeframeSummary,
        settings: AppSettings
    ): AiAnalysisReport = withContext(Dispatchers.IO) {
        // Check if user has configured Gemini API or Backend
        val apiKey = settings.geminiApiKey.trim()
        val customEndpoint = settings.geminiEndpoint.trim()

        if (apiKey.isNotBlank() || customEndpoint.isNotBlank()) {
            try {
                val promptText = buildPrompt(analysis, mtf)
                val responseText = callGeminiApi(apiKey, customEndpoint, promptText)
                if (responseText.isNotBlank()) {
                    return@withContext parseAiResponse(responseText)
                }
            } catch (e: Exception) {
                Log.e("GeminiAdvisor", "Gemini API call failed, falling back to local quantitative engine", e)
            }
        }

        // Algorithmic local institutional quantitative synthesis
        generateLocalQuantitativeReport(analysis, mtf)
    }

    private fun buildPrompt(analysis: TechnicalAnalysisResult, mtf: MultiTimeframeSummary): String {
        return """
            You are a Senior Quantitative Analyst and Gold (XAU/USD) Scalper.
            Current Data:
            - Pair: XAU/USD
            - Timeframe: ${analysis.timeframe.label}
            - Current Price: $${analysis.currentPrice}
            - EMA 9/21/50/200: ${analysis.ema9} / ${analysis.ema21} / ${analysis.ema50} / ${analysis.ema200}
            - RSI 14: ${analysis.rsi14}
            - MACD: ${analysis.macdLine} (Signal: ${analysis.macdSignal}, Hist: ${analysis.macdHistogram})
            - Bollinger Bands: [${analysis.bbLower} - ${analysis.bbMiddle} - ${analysis.bbUpper}]
            - ATR 14: ${analysis.atr14}
            - VWAP: ${analysis.vwap}
            - S/R: Support [${analysis.support1}, ${analysis.support2}], Resistance [${analysis.resistance1}, ${analysis.resistance2}]
            - SMC: ${analysis.smcStructure}, ${analysis.liquiditySweep}, ${analysis.orderBlockStatus}
            - MTF Confluence: ${mtf.confluenceScore}/4 (${mtf.overallBias.title})
            
            Provide a concise institutional scalping brief in Indonesian language with:
            1. Thesis Ringkasan
            2. Arus Order Institusi & Smart Money
            3. Faktor Risiko Kunci
            4. Checklist Eksekusi Scalper
        """.trimIndent()
    }

    private fun callGeminiApi(apiKey: String, customEndpoint: String, prompt: String): String {
        val url = if (customEndpoint.isNotBlank()) {
            customEndpoint
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        }

        val jsonPayload = JSONObject().apply {
            val contents = JSONArray().apply {
                val item = JSONObject().apply {
                    val parts = JSONArray().apply {
                        val part = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(part)
                    }
                    put("parts", parts)
                }
                put(item)
            }
            put("contents", contents)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyString = response.body?.string() ?: return ""

        val root = JSONObject(bodyString)
        val candidates = root.optJSONArray("candidates") ?: return ""
        if (candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        return ""
    }

    private fun parseAiResponse(aiText: String): AiAnalysisReport {
        val lines = aiText.lines().filter { it.isNotBlank() }
        val thesis = lines.take(3).joinToString(" ")
        val flow = lines.drop(3).take(3).joinToString(" ")

        return AiAnalysisReport(
            summaryThesis = thesis.ifBlank { "Analisis cloud AI Gemini mengonfirmasi momentum likuiditas." },
            institutionalOrderFlow = flow.ifBlank { "Arus transaksi didominasi oleh order blocks teruji." },
            keyRiskFactors = listOf(
                "Waspadai lonjakan spread saat rollover London/New York session.",
                "Perhatikan rilis data ekonomi US (CPI, NFP, Fed Minutes).",
                "Disiplin cut loss ketat jika level invalidasi tertembus."
            ),
            executionChecklist = listOf(
                "Tunggu candle close di timeframe entry.",
                "Validasi risk/reward ratio minimum 1:2.",
                "Sesuaikan ukuran lot dengan batas risiko 1-2% modal."
            ),
            isCloudAiGenerated = true
        )
    }

    private fun generateLocalQuantitativeReport(
        analysis: TechnicalAnalysisResult,
        mtf: MultiTimeframeSummary
    ): AiAnalysisReport {
        val bias = analysis.summaryBias
        val thesis = when (bias) {
            com.example.data.model.SignalType.STRONG_BUY, com.example.data.model.SignalType.BUY ->
                "Struktur pasar XAU/USD pada ${analysis.timeframe.label} menunjukkan akumulasi buyer dominan di atas VWAP ($${analysis.vwap}). Alinyemen EMA 9/21/50 dan momentum RSI (${analysis.rsi14}) memvalidasi ekspansi ke resistance $${analysis.resistance1}."
            com.example.data.model.SignalType.STRONG_SELL, com.example.data.model.SignalType.SELL ->
                "Tekanan jual institusional mendorong XAU/USD menembus bawah VWAP ($${analysis.vwap}) dengan konfirmasi EMA death stack. RSI (${analysis.rsi14}) mengarah ke support berikutnya di $${analysis.support1}."
            else ->
                "Harga emas bergerak dalam fase kompresi volatilitas di antara support $${analysis.support1} dan resistance $${analysis.resistance1}. Momentum indikator netral tanpa dominasi order flow searah."
        }

        val orderFlow = when {
            analysis.liquiditySweep.contains("Swept", ignoreCase = true) ->
                "Smart Money Liquidity Sweep: ${analysis.liquiditySweep}. Manipulasi stop-loss selesai, harga bereaksi membalikkan arah."
            analysis.smcStructure.contains("BOS", ignoreCase = true) ->
                "Break of Structure (BOS) terverifikasi dengan peningkatan volume. Order block di level $${analysis.support1} kini bertindak sebagai support kuat."
            else ->
                "Order flow institusional sedang menguji batas Bollinger Band ($${analysis.bbLower} - $${analysis.bbUpper}). Menunggu retest likuiditas terdekat."
        }

        return AiAnalysisReport(
            summaryThesis = thesis,
            institutionalOrderFlow = orderFlow,
            keyRiskFactors = listOf(
                "Volatilitas ATR (${analysis.atr14} pips) mengindikasikan slippage saat news.",
                "Divergensi timeframe minor terhadap H1 jika terjadi false breakout.",
                "Batas risiko maksimal $${String.format("%.2f", analysis.atr14 * 1.5)} pips per posisi scalping."
            ),
            executionChecklist = listOf(
                "Konfirmasi sinyal dengan Multi-Timeframe Confluence (${mtf.confluenceScore}/4).",
                "Pastikan spread broker tidak melebihi 3.0 pips sebelum klik entry.",
                "Gunakan Auto Lot Calculator untuk mengunci risiko tepat 1-2%."
            ),
            isCloudAiGenerated = false
        )
    }
}
