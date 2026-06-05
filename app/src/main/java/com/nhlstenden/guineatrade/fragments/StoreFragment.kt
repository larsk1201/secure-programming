package com.nhlstenden.guineatrade.fragments

import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.HttpClient
import com.nhlstenden.guineatrade.datasources.TotpTokens
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class StoreFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource
    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cartSize = view.findViewById<TextView>(R.id.cart_size)
        val itemGrid = view.findViewById<GridView>(R.id.item_grid)
        val lastUpdate = view.findViewById<TextView>(R.id.last_update)

        if (this@StoreFragment.backpackDatasource.prices == null) {
            lifecycleScope.launch {
                val hasBackpack = async {
                    this@StoreFragment.backpackDatasource.getPrices(this@StoreFragment.userDatasource.tokens.jwtSave)
                }.await()
                if (!hasBackpack) {
                    Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                    lastUpdate.text = getString(R.string.store_no_price_data)
                    return@launch
                }
                lastUpdate.text = this@StoreFragment.backpackDatasource.formatInstantToString()
            }
            Log.d("StoreFragment", this@StoreFragment.backpackDatasource.unusuals.getOrDefault("13", "Not found :/"))
        }
    }
}