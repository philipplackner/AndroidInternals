package com.plcoding.androidinternals

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteCallbackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class MusicService: Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSongIndex = MutableStateFlow(0)

    private val callbacks = RemoteCallbackList<ISongNameChangedCallback>()

    private val lock = Any()

    override fun onCreate() {
        super.onCreate()
        _currentSongIndex
            .onEach { index ->
                val currentSong = songs[index]
                broadcastSongName(currentSong)
            }
            .launchIn(serviceScope)
    }

    private fun broadcastSongName(songName: String) {
        val callbackCount = callbacks.beginBroadcast()
        val failedCallbacks = mutableListOf<ISongNameChangedCallback>()
        for(i in 0 until callbackCount) {
            val callback = callbacks.getBroadcastItem(i)
            try {
                callback.onSongNameChanged(songName)
            } catch(e: Exception) {
                failedCallbacks.add(callback)
                e.printStackTrace()
            }
        }
        callbacks.finishBroadcast()
        failedCallbacks.forEach {
            callbacks.unregister(it)
        }
    }

    fun previous() {
        synchronized(lock) {
            if(_currentSongIndex.value == 0) {
                _currentSongIndex.value = songs.lastIndex
            } else {
                _currentSongIndex.value--
            }
        }
    }

    fun next() {
        synchronized(lock) {
            if(_currentSongIndex.value == songs.lastIndex) {
                _currentSongIndex.value = 0
            } else {
                _currentSongIndex.value++
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return object : IMusicService.Stub() {
            override fun next() {
                this@MusicService.next()
            }

            override fun previous() {
                this@MusicService.previous()
            }

            override fun getCurrentSongName(): String {
                return songs[_currentSongIndex.value]
            }

            override fun registerCallback(callback: ISongNameChangedCallback?) {
                callbacks.register(callback)
            }

            override fun unregisterCallback(callback: ISongNameChangedCallback?) {
                callbacks.unregister(callback)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancelChildren()
    }
}

private val songs = listOf(
    "Blinding Lights – The Weeknd",
    "Shape of You – Ed Sheeran",
    "Bohemian Rhapsody – Queen",
    "Levitating – Dua Lipa",
    "Smells Like Teen Spirit – Nirvana",
    "Rolling in the Deep – Adele",
    "Uptown Funk – Mark Ronson ft. Bruno Mars",
    "Bad Guy – Billie Eilish",
    "Hotel California – Eagles",
    "As It Was – Harry Styles"
)