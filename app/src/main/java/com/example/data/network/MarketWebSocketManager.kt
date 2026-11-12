package com.example.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MarketWebSocketManager(
    private val onPriceUpdate: (bid: Double, ask: Double) -> Unit,
    private val onStatusChange: (Boolean) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(url: String) {
        disconnect()
        if (url.isBlank()) {
            onStatusChange(false)
            return
        }

        try {
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("MarketWS", "WebSocket Connected: $url")
                    onStatusChange(true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val json = JSONObject(text)
                        // Support standard Binance / MT4 / Generic payload formats
                        var bid = 0.0
                        var ask = 0.0

                        if (json.has("b") && json.has("a")) { // Binance format
                            bid = json.optDouble("b", 0.0)
                            ask = json.optDouble("a", 0.0)
                        } else if (json.has("bid") && json.has("ask")) {
                            bid = json.optDouble("bid", 0.0)
                            ask = json.optDouble("ask", 0.0)
                        } else if (json.has("price")) {
                            val price = json.optDouble("price", 0.0)
                            bid = price - 0.15
                            ask = price + 0.15
                        }

                        if (bid > 0.0 && ask > 0.0) {
                            onPriceUpdate(bid, ask)
                        }
                    } catch (e: Exception) {
                        Log.e("MarketWS", "Error parsing WS message", e)
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("MarketWS", "WebSocket failure", t)
                    onStatusChange(false)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("MarketWS", "WebSocket closed: $reason")
                    onStatusChange(false)
                }
            })
        } catch (e: Exception) {
            Log.e("MarketWS", "Error initiating WebSocket", e)
            onStatusChange(false)
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
            webSocket = null
        } catch (_: Exception) {}
        onStatusChange(false)
    }
}
