@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.androidinternals

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
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
) {
    private var service: IMusicService? = null

    private val songChangedCallback = object : ISongNameChangedCallback.Stub() {
        override fun onSongNameChanged(name: String?) {
            if(name != null) {
                _currentSong.value = name
            }
        }
    }

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _currentSong = MutableStateFlow("-")
    val currentSong = _currentSong.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            this@MusicServiceController.service = IMusicService.Stub.asInterface(service)
            this@MusicServiceController.service?.registerCallback(songChangedCallback)
            _currentSong.value = this@MusicServiceController.service?.currentSongName ?: "-"

            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isConnected.value = false
            service = null
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            _isConnected.value = false
            service = null
        }
    }

    fun bind() {
        if(isConnected.value) {
            return
        }
        Intent(context, MusicService::class.java).also {
            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if(!isConnected.value) {
            return
        }
        service?.unregisterCallback(songChangedCallback)

        context.unbindService(serviceConnection)
        _isConnected.value = false
    }

    fun next() {
        service?.next()
    }

    fun previous() {
        service?.previous()
    }
}
