@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.androidinternals

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class MusicServiceController(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private lateinit var service: MusicService

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    val currentSong = isConnected
        .flatMapLatest { isConnected ->
            if(isConnected) {
                service.currentSong
            } else flowOf("-")
        }
        .stateIn(
            coroutineScope,
            SharingStarted.Lazily,
            "-"
        )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicServiceBinder
            this@MusicServiceController.service = binder.getService()
            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isConnected.value = false
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            _isConnected.value = false
        }
    }

    fun bind() {
        Intent(context, MusicService::class.java).also {
            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        context.unbindService(serviceConnection)
        _isConnected.value = false
    }

    fun next() = service.next()

    fun previous() = service.previous()
}