package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity: AppCompatActivity() {

    @Inject lateinit var userDatasource: UserDatasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }
}