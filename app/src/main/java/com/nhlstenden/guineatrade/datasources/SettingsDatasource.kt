package com.nhlstenden.guineatrade.datasources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDatasource @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("user_prefs")
    }

    suspend fun <T> save(key: SettingsKey<T>, value: T) {
        this.dataStore.edit {
            it[key.key] = value
        }
    }

    suspend fun <T> load(key: SettingsKey<T>, default: T): T? {
        return this.dataStore.data.map {
            it[key.key]
        }.first() ?: default
    }
}

class SettingsKey<T>(val key: Preferences.Key<T>)

object SettingsKeys {
    val BIOMETRIC_ENABLED = SettingsKey(booleanPreferencesKey("biometric-enabled"))
    val AUTO_LOGIN_ENABLED = SettingsKey(booleanPreferencesKey("automatic-login"))
}