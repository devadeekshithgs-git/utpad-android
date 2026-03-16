package com.example.gudgum_prod_flow.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Supabase Realtime WebSocket connections.
 * Listens for Postgres changes on gg_ingredients, gg_vendors, gg_flavors, gg_recipes
 * and emits events so ViewModels/Repositories can refresh their caches instantly.
 */
@Singleton
class SupabaseRealtimeManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    companion object {
        private const val TAG = "RealtimeManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Emitted table names whenever a change is detected */
    private val _tableChanged = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val tableChanged: SharedFlow<String> = _tableChanged.asSharedFlow()

    private var isConnected = false

    /**
     * Call once to establish the WebSocket connection and subscribe to all
     * manufacturing-related tables.
     */
    fun connect() {
        if (isConnected) return
        isConnected = true

        scope.launch {
            try {
                val channel = supabaseClient.channel("manufacturing-changes")

                // Listen for changes on each table
                val ingredientChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "gg_ingredients"
                }
                val vendorChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "gg_vendors"
                }
                val flavorChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "gg_flavors"
                }
                val recipeChanges = channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "gg_recipes"
                }

                // Subscribe to the channel (opens WebSocket)
                channel.subscribe()
                Log.d(TAG, "Realtime channel subscribed successfully")

                // Collect each flow and emit the table name
                launch {
                    ingredientChanges.collect { action ->
                        Log.d(TAG, "gg_ingredients changed: ${action::class.simpleName}")
                        _tableChanged.emit("gg_ingredients")
                    }
                }
                launch {
                    vendorChanges.collect { action ->
                        Log.d(TAG, "gg_vendors changed: ${action::class.simpleName}")
                        _tableChanged.emit("gg_vendors")
                    }
                }
                launch {
                    flavorChanges.collect { action ->
                        Log.d(TAG, "gg_flavors changed: ${action::class.simpleName}")
                        _tableChanged.emit("gg_flavors")
                    }
                }
                launch {
                    recipeChanges.collect { action ->
                        Log.d(TAG, "gg_recipes changed: ${action::class.simpleName}")
                        _tableChanged.emit("gg_recipes")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Realtime connection error", e)
                isConnected = false
            }
        }
    }
}
