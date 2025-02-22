package com.plcoding.androidinternals

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MusicApplication: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lateinit var musicServiceController: MusicServiceController

    override fun onCreate() {
        super.onCreate()
        musicServiceController = MusicServiceController(
            context = this,
            coroutineScope = applicationScope
        )
    }
}