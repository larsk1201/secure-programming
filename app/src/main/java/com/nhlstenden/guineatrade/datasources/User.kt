package com.nhlstenden.guineatrade.datasources

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class User @Inject constructor() {
    val username = "John Doe"
    val balance = 49650
}