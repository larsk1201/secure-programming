package com.nhlstenden.guineatrade.datasources

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class User @Inject constructor() {
    var username = ""
    var email = ""
    var balance = 49650

    fun login(email: String, password: String) {
//        TODO: Make API call
//        http.post(guineatrade.com/api/v1/auth/login, {"email": email, "password": password})
        this.username = email
        this.email = email
        Log.d("UserSingleton", password)
    }
}