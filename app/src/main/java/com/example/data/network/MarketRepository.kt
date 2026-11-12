package com.example.data.network

import com.example.data.datastore.AppSettings
import com.example.data.model.Candle
import com.example.data.model.ConnectionState
import com.example.data.model.TickerPrice
import com.example.data.model.Timeframe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Random
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MarketRepository(
    private val scope: CoroutineScope
) {
    private val _ticker = MutableStateFlow(
        TickerPrice(
            symbol = "XAU/USD",
            bid = 2684.20,
            ask = 2684.45,
            spread = 2.5,
            change = +14.60,
            changePercent = +0.55,
            high24h = 2695.80,
            low24h = 2668.40,
            connectionState = ConnectionState.DEMO
        )
    )
    val ticker: StateFlow<TickerPrice> = _ticker.asStateFlow()

    private val _m1Candles = MutableStateFlow<List<Candle>>(emptyList())
    val m1Candles: StateFlow<List<Candle>> = _m1Candles.asStateFlow()

    private val _m5Candles = MutableStateFlow<List<Candle>>(emptyList())
    val m5Candles: StateFlow<List<Candle>> = _m5Candles.asStateFlow()

    private val _m15Candles = MutableStateFlow<List<Candle>>(emptyList())
    val m15Candles: StateFlow<List<Candle>> = _m15Candles.asStateFlow()

    private val _h1Candles = MutableStateFlow<List<Candle>>(emptyList())
    val h1Candles: StateFlow<List<Candle>> = _h1Candles.asStateFlow()

    private var activeJob: Job? = null
    private var wsManager: MarketWebSocketManager? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.binance.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MarketApiService::class.java)
    private val random = Random()

    init {
        // Seed initial historical candles based on realistic benchmark gold price (2684.00)
        seedInitialCandles(2684.00)
    }

    private fun seedInitialCandles(basePrice: Double) {
        val count = 45
        var current = basePrice - 8.0

        val m1 = mutableListOf<Candle>()
        val m5 = mutableListOf<Candle>()
        val m15 = mutableListOf<Candle>()
        val h1 = mutableListOf<Candle>()

        val now = System.currentTimeMillis()

        for (i in 0 until count) {
            val delta = (random.nextDouble() - 0.48) * 1.8
            val o = current
            current += delta
            val c = current
            val h = max(o, c) + (random.nextDouble() * 0.9)
            val l = min(o, c) - (random.nextDouble() * 0.9)
            val v = 80.0 + (random.nextDouble() * 140.0)

            m1.add(Candle(now - ((count - i) * 60 * 1000L), round(o), round(h), round(l), round(c), round(v)))
            m5.add(Candle(now - ((count - i) * 5 * 60 * 1000L), round(o - 1.2), round(h + 1.5), round(l - 1.5), round(c + 0.8), round(v * 2.5)))
            m15.add(Candle(now - ((count - i) * 15 * 60 * 1000L), round(o - 2.5), round(h + 2.8), round(l - 2.8), round(c + 1.2), round(v * 4.0)))
            h1.add(Candle(now - ((count - i) * 60 * 60 * 1000L), round(o - 5.0), round(h + 6.0), round(l - 6.0), round(c + 3.0), round(v * 10.0)))
        }

        _m1Candles.value = m1
        _m5Candles.value = m5
        _m15Candles.value = m15
        _h1Candles.value = h1
    }

    fun start(settings: AppSettings) {
        activeJob?.cancel()
        wsManager?.disconnect()

        if (settings.isDemoMode) {
            startDemoSimulation()
        } else {
            startLiveFeed(settings)
        }
    }

    private fun startDemoSimulation() {
        activeJob = scope.launch(Dispatchers.Default) {
            _ticker.value = _ticker.value.copy(connectionState = ConnectionState.DEMO)

            var price = _ticker.value.ask
            while (isActive) {
                delay(1200L) // Realistic gold scalp tick interval
                val tickDrift = (random.nextGaussian() * 0.35) // Micro price variance
                price = (price + tickDrift).coerceIn(2600.0, 2780.0)
                val spread = 1.8 + (random.nextDouble() * 1.4) // Spread 1.8 - 3.2 pips
                val bid = round(price)
                val ask = round(price + (spread * 0.1))

                val open24 = 2670.00
                val change = round(price - open24)
                val changePct = round((change / open24) * 100.0)

                _ticker.value = TickerPrice(
                    symbol = "XAU/USD",
                    bid = bid,
                    ask = ask,
                    spread = round(spread),
                    change = change,
                    changePercent = changePct,
                    high24h = max(_ticker.value.high24h, ask),
                    low24h = min(_ticker.value.low24h, bid),
                    timestamp = System.currentTimeMillis(),
                    connectionState = ConnectionState.DEMO
                )

                // Update current M1 candle or append new
                updateCandlesWithTick(price)
            }
        }
    }

    private fun startLiveFeed(settings: AppSettings) {
        activeJob = scope.launch(Dispatchers.IO) {
            // Check if user provided WebSocket
            if (settings.webSocketUrl.isNotBlank()) {
                wsManager = MarketWebSocketManager(
                    onPriceUpdate = { bid, ask ->
                        val spread = abs(ask - bid) * 10.0 // in pips
                        _ticker.value = _ticker.value.copy(
                            bid = round(bid),
                            ask = round(ask),
                            spread = round(spread),
                            connectionState = ConnectionState.LIVE,
                            timestamp = System.currentTimeMillis()
                        )
                        updateCandlesWithTick((bid + ask) / 2.0)
                    },
                    onStatusChange = { isConnected ->
                        if (!isConnected) {
                            _ticker.value = _ticker.value.copy(connectionState = ConnectionState.OFFLINE)
                        }
                    }
                )
                wsManager?.connect(settings.webSocketUrl)
            } else {
                // Polling REST API or Binance fallback
                while (isActive) {
                    try {
                        val response = if (settings.marketApiUrl.isNotBlank()) {
                            apiService.getGenericGoldQuote(
                                url = settings.marketApiUrl,
                                apiKey = settings.marketApiKey.ifBlank { null }
                            )
                        } else {
                            null
                        }

                        if (response != null && response.isSuccessful && response.body() != null) {
                            val data = response.body()!!
                            val bid = data.bid ?: (data.price ?: 2684.0) - 0.2
                            val ask = data.ask ?: (data.price ?: 2684.0) + 0.2
                            val spread = abs(ask - bid) * 10.0

                            _ticker.value = TickerPrice(
                                symbol = "XAU/USD",
                                bid = round(bid),
                                ask = round(ask),
                                spread = round(spread),
                                change = data.change ?: 0.0,
                                changePercent = data.changePercent ?: 0.0,
                                high24h = data.high ?: ask,
                                low24h = data.low ?: bid,
                                timestamp = System.currentTimeMillis(),
                                connectionState = ConnectionState.LIVE
                            )
                            updateCandlesWithTick((bid + ask) / 2.0)
                        } else {
                            // Try Binance gold benchmark (PAXGUSDT)
                            val binanceRes = apiService.getBinanceTicker()
                            if (binanceRes.isSuccessful && binanceRes.body() != null) {
                                val b = binanceRes.body()!!
                                val bid = b.bidPrice?.toDoubleOrNull() ?: 2684.0
                                val ask = b.askPrice?.toDoubleOrNull() ?: 2684.5
                                val change = b.priceChange?.toDoubleOrNull() ?: 10.0
                                val changePct = b.priceChangePercent?.toDoubleOrNull() ?: 0.45
                                val high = b.highPrice?.toDoubleOrNull() ?: 2690.0
                                val low = b.lowPrice?.toDoubleOrNull() ?: 2665.0
                                val spread = abs(ask - bid) * 10.0

                                _ticker.value = TickerPrice(
                                    symbol = "XAU/USD",
                                    bid = round(bid),
                                    ask = round(ask),
                                    spread = round(spread),
                                    change = round(change),
                                    changePercent = round(changePct),
                                    high24h = round(high),
                                    low24h = round(low),
                                    timestamp = System.currentTimeMillis(),
                                    connectionState = ConnectionState.LIVE
                                )
                                updateCandlesWithTick((bid + ask) / 2.0)
                            } else {
                                _ticker.value = _ticker.value.copy(connectionState = ConnectionState.OFFLINE)
                            }
                        }
                    } catch (e: Exception) {
                        _ticker.value = _ticker.value.copy(connectionState = ConnectionState.OFFLINE)
                    }

                    delay(3000L)
                }
            }
        }
    }

    private fun updateCandlesWithTick(currentPrice: Double) {
        val now = System.currentTimeMillis()
        _m1Candles.value = updateCandleList(_m1Candles.value, currentPrice, 60 * 1000L, now)
        _m5Candles.value = updateCandleList(_m5Candles.value, currentPrice, 5 * 60 * 1000L, now)
        _m15Candles.value = updateCandleList(_m15Candles.value, currentPrice, 15 * 60 * 1000L, now)
        _h1Candles.value = updateCandleList(_h1Candles.value, currentPrice, 60 * 60 * 1000L, now)
    }

    private fun updateCandleList(
        list: List<Candle>,
        price: Double,
        intervalMs: Long,
        now: Long
    ): List<Candle> {
        if (list.isEmpty()) return list
        val mutable = list.toMutableList()
        val last = mutable.last()

        if (now - last.timestamp < intervalMs) {
            // Update last candle
            val updated = last.copy(
                high = max(last.high, price),
                low = min(last.low, price),
                close = price,
                volume = last.volume + (random.nextDouble() * 2.5)
            )
            mutable[mutable.size - 1] = updated
        } else {
            // New candle started
            val newCandle = Candle(
                timestamp = now,
                open = last.close,
                high = max(last.close, price),
                low = min(last.close, price),
                close = price,
                volume = 20.0 + (random.nextDouble() * 30.0)
            )
            mutable.add(newCandle)
            if (mutable.size > 80) {
                mutable.removeAt(0)
            }
        }
        return mutable
    }

    fun getCandlesForTimeframe(timeframe: Timeframe): List<Candle> {
        return when (timeframe) {
            Timeframe.M1 -> _m1Candles.value
            Timeframe.M5 -> _m5Candles.value
            Timeframe.M15 -> _m15Candles.value
            Timeframe.H1 -> _h1Candles.value
        }
    }

    private fun round(value: Double): Double {
        return (value * 100.0).toLong() / 100.0
    }
}
