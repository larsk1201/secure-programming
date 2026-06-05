package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

        if (this@StoreFragment.backpackDatasource.prices == null) {
            lifecycleScope.launch {
                Log.d("StoreFragment", this@StoreFragment::backpackDatasource.isInitialized.toString())
                Log.d("StoreFragment", this@StoreFragment::userDatasource.isInitialized.toString())
                val hasBackpack = async {
                    this@StoreFragment.backpackDatasource.getPrices(this@StoreFragment.userDatasource.tokens.jwtSave)
                }.await()
                if (!hasBackpack) {
                    Log.d("StoreFragment", "Unable to able to able")
                    return@launch
                }
                Log.d("StoreFragment",
                    this@StoreFragment.backpackDatasource.prices!!.timestamp.toString()
                )
            }
            Log.d("StoreFragment", this@StoreFragment.backpackDatasource.unusuals.getOrDefault("13", "Not found :/"))
        }
    }
}