package com.nhlstenden.guineatrade.datasources

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore("user_prefs")

@Singleton
class SettingsDatasource @Inject constructor(
    @ApplicationContext private val context: Context
){
    suspend fun <T> save(key: SettingsKey<T>, value: T) {
        this.context.dataStore.edit {
            it[key.key] = value
        }
    }

    suspend fun <T> load(key: SettingsKey<T>, default: T): T {
        return this.context.dataStore.data.map {
            it[key.key]
        }.first() ?: default
    }
}

class SettingsKey<T>(val key: Preferences.Key<T>)

object SettingsKeys {
    val BIOMETRIC_ENABLED = SettingsKey(booleanPreferencesKey("biometric-enabled"))
    val AUTO_LOGIN_ENABLED = SettingsKey(booleanPreferencesKey("automatic-login"))
}