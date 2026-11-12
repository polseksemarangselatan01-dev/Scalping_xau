package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gold_scalping_settings")

data class AppSettings(
    val isDemoMode: Boolean = true,
    val marketApiUrl: String = "",
    val marketApiKey: String = "",
    val webSocketUrl: String = "",
    val geminiApiKey: String = "",
    val geminiEndpoint: String = "",
    val contractSize: Double = 100.0,
    val defaultBalance: Double = 5000.0,
    val defaultRiskPercent: Double = 1.0,
    val notifyBuySell: Boolean = true,
    val notifyTrendChange: Boolean = true,
    val notifyBreakout: Boolean = true,
    val notifyHighSpread: Boolean = true,
    val notifyEconomicNews: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {
    companion object {
        val KEY_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val KEY_MARKET_API_URL = stringPreferencesKey("market_api_url")
        val KEY_MARKET_API_KEY = stringPreferencesKey("market_api_key")
        val KEY_WEBSOCKET_URL = stringPreferencesKey("websocket_url")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GEMINI_ENDPOINT = stringPreferencesKey("gemini_endpoint")
        val KEY_CONTRACT_SIZE = doublePreferencesKey("contract_size")
        val KEY_DEFAULT_BALANCE = doublePreferencesKey("default_balance")
        val KEY_DEFAULT_RISK = doublePreferencesKey("default_risk_percent")
        val KEY_NOTIFY_BUY_SELL = booleanPreferencesKey("notify_buy_sell")
        val KEY_NOTIFY_TREND = booleanPreferencesKey("notify_trend")
        val KEY_NOTIFY_BREAKOUT = booleanPreferencesKey("notify_breakout")
        val KEY_NOTIFY_HIGH_SPREAD = booleanPreferencesKey("notify_high_spread")
        val KEY_NOTIFY_NEWS = booleanPreferencesKey("notify_news")
        val KEY_SOUND = booleanPreferencesKey("is_sound_enabled")
        val KEY_VIBRATION = booleanPreferencesKey("is_vibration_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            isDemoMode = preferences[KEY_DEMO_MODE] ?: true,
            marketApiUrl = preferences[KEY_MARKET_API_URL] ?: "",
            marketApiKey = preferences[KEY_MARKET_API_KEY] ?: "",
            webSocketUrl = preferences[KEY_WEBSOCKET_URL] ?: "",
            geminiApiKey = preferences[KEY_GEMINI_API_KEY] ?: "",
            geminiEndpoint = preferences[KEY_GEMINI_ENDPOINT] ?: "",
            contractSize = preferences[KEY_CONTRACT_SIZE] ?: 100.0,
            defaultBalance = preferences[KEY_DEFAULT_BALANCE] ?: 5000.0,
            defaultRiskPercent = preferences[KEY_DEFAULT_RISK] ?: 1.0,
            notifyBuySell = preferences[KEY_NOTIFY_BUY_SELL] ?: true,
            notifyTrendChange = preferences[KEY_NOTIFY_TREND] ?: true,
            notifyBreakout = preferences[KEY_NOTIFY_BREAKOUT] ?: true,
            notifyHighSpread = preferences[KEY_NOTIFY_HIGH_SPREAD] ?: true,
            notifyEconomicNews = preferences[KEY_NOTIFY_NEWS] ?: true,
            isSoundEnabled = preferences[KEY_SOUND] ?: true,
            isVibrationEnabled = preferences[KEY_VIBRATION] ?: true
        )
    }

    suspend fun updateDemoMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DEMO_MODE] = enabled }
    }

    suspend fun updateApiSettings(
        marketUrl: String,
        marketKey: String,
        wsUrl: String,
        geminiKey: String,
        geminiEndpoint: String
    ) {
        context.dataStore.edit {
            it[KEY_MARKET_API_URL] = marketUrl
            it[KEY_MARKET_API_KEY] = marketKey
            it[KEY_WEBSOCKET_URL] = wsUrl
            it[KEY_GEMINI_API_KEY] = geminiKey
            it[KEY_GEMINI_ENDPOINT] = geminiEndpoint
        }
    }

    suspend fun updateCalculatorDefaults(balance: Double, riskPercent: Double, contractSize: Double) {
        context.dataStore.edit {
            it[KEY_DEFAULT_BALANCE] = balance
            it[KEY_DEFAULT_RISK] = riskPercent
            it[KEY_CONTRACT_SIZE] = contractSize
        }
    }

    suspend fun updateNotificationSettings(
        buySell: Boolean,
        trend: Boolean,
        breakout: Boolean,
        highSpread: Boolean,
        news: Boolean,
        sound: Boolean,
        vibration: Boolean
    ) {
        context.dataStore.edit {
            it[KEY_NOTIFY_BUY_SELL] = buySell
            it[KEY_NOTIFY_TREND] = trend
            it[KEY_NOTIFY_BREAKOUT] = breakout
            it[KEY_NOTIFY_HIGH_SPREAD] = highSpread
            it[KEY_NOTIFY_NEWS] = news
            it[KEY_SOUND] = sound
            it[KEY_VIBRATION] = vibration
        }
    }
}
