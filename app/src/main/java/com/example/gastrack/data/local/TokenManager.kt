package com.example.gastrack.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gastrack_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type") // "employee" | "customer"
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val WAREHOUSE_ID_KEY = intPreferencesKey("warehouse_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userTypeFlow: Flow<String?> = context.dataStore.data.map { it[USER_TYPE_KEY] }

    suspend fun saveSession(
        token: String,
        userType: String,
        userName: String,
        userId: Int,
        warehouseId: Int? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_TYPE_KEY] = userType
            prefs[USER_NAME_KEY] = userName
            prefs[USER_ID_KEY] = userId
            warehouseId?.let { prefs[WAREHOUSE_ID_KEY] = it }
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[TOKEN_KEY]
    suspend fun getUserName(): String? = context.dataStore.data.first()[USER_NAME_KEY]
    suspend fun getUserId(): Int? = context.dataStore.data.first()[USER_ID_KEY]
    suspend fun getUserType(): String? = context.dataStore.data.first()[USER_TYPE_KEY]
    suspend fun getWarehouseId(): Int? = context.dataStore.data.first()[WAREHOUSE_ID_KEY]

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}