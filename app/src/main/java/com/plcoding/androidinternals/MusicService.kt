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

    private val clients = mutableListOf<Messenger>()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var serverMessenger: Messenger

    private val _currentSongIndex = MutableStateFlow(0)

    override fun onCreate() {
        super.onCreate()
        _currentSongIndex
            .onEach { index ->
                val currentSong = songs[index]
                clients.forEach { client ->
                    sendCurrentSongName(client, currentSong)
                }
            }
            .launchIn(serviceScope)
    }

    fun previous() {
        if(_currentSongIndex.value == 0) {
            _currentSongIndex.value = songs.lastIndex
        } else {
            _currentSongIndex.value--
        }
    }

    fun next() {
        if(_currentSongIndex.value == songs.lastIndex) {
            _currentSongIndex.value = 0
        } else {
            _currentSongIndex.value++
        }
    }

    private fun sendCurrentSongName(clientMessenger: Messenger, songName: String) {
        val message = Message.obtain(
            null,
            MusicServiceClientEvent.SONG_CHANGED.what
        )
        message.data = Bundle().apply {
            putString("KEY_SONG_NAME", songName)
        }

        try {
            clientMessenger.send(message)
        } catch(e: DeadObjectException) {
            clients.remove(clientMessenger)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        serverMessenger = Messenger(
            IncomingHandler(
                service = object : MusicServiceInterface {
                    override val clients: MutableList<Messenger>
                        get() = this@MusicService.clients

                    override fun next() {
                        this@MusicService.next()
                    }

                    override fun previous() {
                        this@MusicService.previous()
                    }

                    override fun sendCurrentSongName(clientMessenger: Messenger) {
                        this@MusicService.sendCurrentSongName(
                            clientMessenger = clientMessenger,
                            songName = songs[_currentSongIndex.value]
                        )
                    }
                }
            )
        )
        return serverMessenger.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancelChildren()
    }

    class IncomingHandler(
        private val service: MusicServiceInterface
    ): Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            val command = MusicServiceCommand.entries.find { it.what == msg.what }
                ?: throw IllegalArgumentException("Invalid command.")
            when(command) {
                MusicServiceCommand.NEXT -> service.next()
                MusicServiceCommand.PREVIOUS -> service.previous()
                MusicServiceCommand.REGISTER -> {
                    val clientMessenger = msg.replyTo
                    if(clientMessenger != null && !service.clients.contains(clientMessenger)) {
                        service.clients.add(clientMessenger)
                        service.sendCurrentSongName(clientMessenger)
                    }
                }
                MusicServiceCommand.UNREGISTER -> {
                    service.clients.remove(msg.replyTo)
                }
            }
        }
    }

    interface MusicServiceInterface {
        val clients: MutableList<Messenger>
        fun next()
        fun previous()
        fun sendCurrentSongName(clientMessenger: Messenger)
    }
}

enum class MusicServiceCommand(val what: Int) {
    NEXT(0),
    PREVIOUS(1),
    REGISTER(2),
    UNREGISTER(3),
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