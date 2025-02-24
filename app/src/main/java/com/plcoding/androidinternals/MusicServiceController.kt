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
    private var serverMessenger: Messenger? = null
    private val clientMessenger = Messenger(
        ClientIncomingHandler(
            onSongChanged = { _currentSong.value = it }
        )
    )

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _currentSong = MutableStateFlow("-")
    val currentSong = _currentSong.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serverMessenger = Messenger(service)

            val msg = Message.obtain(null, MusicServiceCommand.REGISTER.what)
            msg.replyTo = clientMessenger
            serverMessenger?.send(msg)

            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isConnected.value = false
            serverMessenger = null
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            _isConnected.value = false
            serverMessenger = null
        }
    }

    fun bind() {
        Intent(context, MusicService::class.java).also {
            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        val msg = Message.obtain(null, MusicServiceCommand.UNREGISTER.what)
        msg.replyTo = clientMessenger
        serverMessenger?.send(msg)

        context.unbindService(serviceConnection)
        _isConnected.value = false
    }

    fun next() {
        val msg = Message.obtain(null, MusicServiceCommand.NEXT.what)
        msg.replyTo = clientMessenger
        serverMessenger?.send(msg)
    }

    fun previous() {
        val msg = Message.obtain(null, MusicServiceCommand.PREVIOUS.what)
        msg.replyTo = clientMessenger
        serverMessenger?.send(msg)
    }

    class ClientIncomingHandler(
        private val onSongChanged: (String) -> Unit
    ): Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            val event = MusicServiceClientEvent.entries.find { it.what == msg.what }
                ?: throw IllegalArgumentException("Invalid event.")
            when(event) {
                MusicServiceClientEvent.SONG_CHANGED -> {
                    val songName = msg.data.getString("KEY_SONG_NAME") ?: return
                    onSongChanged(songName)
                }
            }
        }
    }
}

enum class MusicServiceClientEvent(val what: Int) {
    SONG_CHANGED(0)
}