package com.nhlstenden.guineatrade.datasources

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserDatasource @Inject constructor() {
    var username = ""
    var email = ""
    var phone = ""
    var balance = 49650

    fun login(email: String, password: String): Boolean {
//        TODO: Make API call
        this.username = email
        this.email = email
        Log.d("UserSingleton", password)
        Log.d("UserSingleton", email)

        return true
    }

    fun signup(email: String, password: String, passwordConfirm: String, phoneNumber: String): Boolean {
        this.username = email
        this.email = email
        this.phone = phoneNumber
//        TODO: Make API call

        return true
    }

    @Provides
    @Singleton
    fun provideUser(): UserDatasource = UserDatasource()
}