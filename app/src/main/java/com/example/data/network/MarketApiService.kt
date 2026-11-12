package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class GoldQuoteResponse(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "bid") val bid: Double? = null,
    @Json(name = "ask") val ask: Double? = null,
    @Json(name = "high_price") val high: Double? = null,
    @Json(name = "low_price") val low: Double? = null,
    @Json(name = "change") val change: Double? = null,
    @Json(name = "change_percent") val changePercent: Double? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class BinanceTickerResponse(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "bidPrice") val bidPrice: String? = null,
    @Json(name = "askPrice") val askPrice: String? = null,
    @Json(name = "lastPrice") val lastPrice: String? = null,
    @Json(name = "priceChange") val priceChange: String? = null,
    @Json(name = "priceChangePercent") val priceChangePercent: String? = null,
    @Json(name = "highPrice") val highPrice: String? = null,
    @Json(name = "lowPrice") val lowPrice: String? = null,
    @Json(name = "volume") val volume: String? = null
)

interface MarketApiService {
    @GET
    suspend fun getGenericGoldQuote(
        @Url url: String,
        @Header("x-access-token") apiKey: String? = null
    ): Response<GoldQuoteResponse>

    @GET("api/v3/ticker/24hr")
    suspend fun getBinanceTicker(
        @Query("symbol") symbol: String = "PAXGUSDT" // Pax Gold / USDT tracking real 1oz gold
    ): Response<BinanceTickerResponse>
}
