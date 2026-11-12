package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.datastore.AppSettings
import com.example.data.datastore.SettingsRepository
import com.example.data.model.CalculatorResult
import com.example.data.model.Candle
import com.example.data.model.ConnectionState
import com.example.data.model.MultiTimeframeSummary
import com.example.data.model.TechnicalAnalysisResult
import com.example.data.model.TickerPrice
import com.example.data.model.Timeframe
import com.example.data.model.TradeJournalEntity
import com.example.data.model.TradingSignal
import com.example.data.network.MarketRepository
import com.example.domain.analyzer.AiAnalysisReport
import com.example.domain.analyzer.GeminiTradingAdvisor
import com.example.domain.analyzer.MultiTimeframeEngine
import com.example.domain.analyzer.SignalGenerator
import com.example.domain.calculator.LotSizeCalculator
import com.example.domain.notification.MarketAlertManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val journalDao = database.journalDao()
    private val settingsRepo = SettingsRepository(application)
    private val alertManager = MarketAlertManager(application)
    private val geminiAdvisor = GeminiTradingAdvisor()

    private val marketRepo = MarketRepository(viewModelScope)

    val ticker: StateFlow<TickerPrice> = marketRepo.ticker

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    private val _selectedTimeframe = MutableStateFlow(Timeframe.M5)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _currentCandles = MutableStateFlow<List<Candle>>(emptyList())
    val currentCandles: StateFlow<List<Candle>> = _currentCandles.asStateFlow()

    private val _currentAnalysis = MutableStateFlow<TechnicalAnalysisResult?>(null)
    val currentAnalysis: StateFlow<TechnicalAnalysisResult?> = _currentAnalysis.asStateFlow()

    private val _mtfSummary = MutableStateFlow<MultiTimeframeSummary?>(null)
    val mtfSummary: StateFlow<MultiTimeframeSummary?> = _mtfSummary.asStateFlow()

    private val _latestSignal = MutableStateFlow<TradingSignal?>(null)
    val latestSignal: StateFlow<TradingSignal?> = _latestSignal.asStateFlow()

    private val _aiReport = MutableStateFlow<AiAnalysisReport?>(null)
    val aiReport: StateFlow<AiAnalysisReport?> = _aiReport.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _calculatorResult = MutableStateFlow(
        LotSizeCalculator.calculate(
            balance = 5000.0,
            equity = 5000.0,
            riskPercent = 1.0,
            entryPrice = 2684.00,
            stopLossPrice = 2681.50,
            takeProfit1 = 2687.75,
            takeProfit2 = 2690.25,
            takeProfit3 = 2694.00,
            contractSize = 100.0,
            currency = "USD"
        )
    )
    val calculatorResult: StateFlow<CalculatorResult> = _calculatorResult.asStateFlow()

    val journalTrades: StateFlow<List<TradeJournalEntity>> = journalDao.getAllTrades().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    private var lastNotifiedSignalId: String? = null

    init {
        // Observe settings to initialize/restart market feed
        viewModelScope.launch {
            settings.collect { appSettings ->
                marketRepo.start(appSettings)
            }
        }

        // Periodically refresh technical calculations and MTF
        viewModelScope.launch(Dispatchers.Default) {
            marketRepo.ticker.collect {
                refreshAnalytics()
            }
        }
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
        refreshAnalytics()
    }

    private fun refreshAnalytics() {
        val tf = _selectedTimeframe.value
        val candles = marketRepo.getCandlesForTimeframe(tf)
        _currentCandles.value = candles

        val m1 = marketRepo.getCandlesForTimeframe(Timeframe.M1)
        val m5 = marketRepo.getCandlesForTimeframe(Timeframe.M5)
        val m15 = marketRepo.getCandlesForTimeframe(Timeframe.M15)
        val h1 = marketRepo.getCandlesForTimeframe(Timeframe.H1)

        val mtf = MultiTimeframeEngine.evaluateMultiTimeframe(m1, m5, m15, h1)
        _mtfSummary.value = mtf

        val analysis = MultiTimeframeEngine.analyzeSingleTimeframe(tf, candles)
        _currentAnalysis.value = analysis

        // Auto generate/update current signal
        val sig = SignalGenerator.generateSignal(tf, candles, mtf)
        _latestSignal.value = sig

        // Check if alert should trigger
        if (sig != null && sig.id != lastNotifiedSignalId && sig.type != com.example.data.model.SignalType.WAIT) {
            lastNotifiedSignalId = sig.id
            alertManager.notifySignal(sig, settings.value)
        }

        // Check high spread alert (> 3.5 pips)
        if (ticker.value.spread > 3.5) {
            alertManager.notifyMarketAlert(
                "SPREAD",
                "Spread XAU/USD tinggi: ${ticker.value.spread} pips. Tunda eksekusi scalping.",
                settings.value
            )
        }
    }

    fun generateSignalManually() {
        refreshAnalytics()
    }

    fun requestAiDeepAnalysis() {
        val analysis = _currentAnalysis.value ?: return
        val mtf = _mtfSummary.value ?: return

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val report = geminiAdvisor.generateDeepAnalysis(analysis, mtf, settings.value)
                _aiReport.value = report
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateDemoMode(enabled)
        }
    }

    fun saveApiSettings(
        marketUrl: String,
        marketKey: String,
        wsUrl: String,
        geminiKey: String,
        geminiEndpoint: String
    ) {
        viewModelScope.launch {
            settingsRepo.updateApiSettings(marketUrl, marketKey, wsUrl, geminiKey, geminiEndpoint)
        }
    }

    fun updateNotificationToggles(
        buySell: Boolean,
        trend: Boolean,
        breakout: Boolean,
        highSpread: Boolean,
        news: Boolean,
        sound: Boolean,
        vibration: Boolean
    ) {
        viewModelScope.launch {
            settingsRepo.updateNotificationSettings(
                buySell, trend, breakout, highSpread, news, sound, vibration
            )
        }
    }

    fun calculateLot(
        balance: Double,
        equity: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLossPrice: Double,
        takeProfit1: Double = 0.0,
        takeProfit2: Double = 0.0,
        takeProfit3: Double = 0.0,
        contractSize: Double = 100.0,
        currency: String = "USD"
    ) {
        _calculatorResult.value = LotSizeCalculator.calculate(
            balance = balance,
            equity = equity,
            riskPercent = riskPercent,
            entryPrice = entryPrice,
            stopLossPrice = stopLossPrice,
            takeProfit1 = takeProfit1,
            takeProfit2 = takeProfit2,
            takeProfit3 = takeProfit3,
            contractSize = contractSize,
            currency = currency
        )
    }

    fun prefillCalculatorFromSignal(signal: TradingSignal) {
        val currentBalance = _calculatorResult.value.balance
        val currentRisk = _calculatorResult.value.riskPercent
        val currentContract = _calculatorResult.value.contractSize
        calculateLot(
            balance = currentBalance,
            equity = currentBalance,
            riskPercent = currentRisk,
            entryPrice = signal.entryPrice,
            stopLossPrice = signal.stopLoss,
            takeProfit1 = signal.takeProfit1,
            takeProfit2 = signal.takeProfit2,
            takeProfit3 = signal.takeProfit3,
            contractSize = currentContract,
            currency = "USD"
        )
    }

    fun saveTradeToJournal(
        signal: TradingSignal,
        lotSize: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val trade = TradeJournalEntity(
                pair = signal.pair,
                signalType = signal.type.name,
                timeframe = signal.timeframe.label,
                entryPrice = signal.entryPrice,
                stopLoss = signal.stopLoss,
                takeProfit = signal.takeProfit1,
                lotSize = lotSize,
                status = "OPEN",
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            journalDao.insertTrade(trade)
        }
    }

    fun updateTradeStatus(trade: TradeJournalEntity, newStatus: String, exitPrice: Double) {
        viewModelScope.launch {
            val isBuy = trade.signalType.contains("BUY", ignoreCase = true)
            val pnlPips = if (isBuy) (exitPrice - trade.entryPrice) else (trade.entryPrice - exitPrice)
            val pnlDollar = pnlPips * trade.lotSize * 100.0

            val updated = trade.copy(
                status = newStatus,
                exitPrice = exitPrice,
                pnl = (pnlDollar * 100).toLong() / 100.0,
                closedAt = System.currentTimeMillis()
            )
            journalDao.updateTrade(updated)
        }
    }

    fun deleteTrade(trade: TradeJournalEntity) {
        viewModelScope.launch {
            journalDao.deleteTrade(trade)
        }
    }

    fun clearJournal() {
        viewModelScope.launch {
            journalDao.clearAllTrades()
        }
    }
}
