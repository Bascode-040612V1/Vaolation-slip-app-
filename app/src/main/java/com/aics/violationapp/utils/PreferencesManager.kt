package com.aics.violationapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.aics.violationapp.data.model.User

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("violation_app_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_IP_ADDRESS = "ip_address"
        private const val KEY_PORT = "port"
        private const val DEFAULT_IP = "192.168.1.4"
        private const val DEFAULT_PORT = "8080"
    }
    
    fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.role)
            apply()
        }
    }
    
    fun getUser(): User? {
        return if (isLoggedIn()) {
            User(
                id = sharedPreferences.getInt(KEY_USER_ID, 0),
                username = sharedPreferences.getString(KEY_USERNAME, "") ?: "",
                email = sharedPreferences.getString(KEY_EMAIL, "") ?: "",
                role = sharedPreferences.getString(KEY_ROLE, "guard") ?: "guard"
            )
        } else null
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_EMAIL)
            remove(KEY_ROLE)
            apply()
        }
    }
    
    fun setServerConfig(ipAddress: String, port: String) {
        sharedPreferences.edit().apply {
            putString(KEY_IP_ADDRESS, ipAddress)
            putString(KEY_PORT, port)
            putString(KEY_BASE_URL, "http://$ipAddress:$port/violation_api/")
            apply()
        }
    }
    
    fun getBaseUrl(): String {
        val ip = sharedPreferences.getString(KEY_IP_ADDRESS, DEFAULT_IP) ?: DEFAULT_IP
        val port = sharedPreferences.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
        return sharedPreferences.getString(KEY_BASE_URL, "http://$ip:$port/violation_api/") 
            ?: "http://$ip:$port/violation_api/"
    }
    
    fun getIpAddress(): String {
        return sharedPreferences.getString(KEY_IP_ADDRESS, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    fun getPort(): String {
        return sharedPreferences.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
    }
}
