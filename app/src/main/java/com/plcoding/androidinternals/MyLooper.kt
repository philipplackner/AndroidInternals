package com.plcoding.androidinternals

import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class MyLooper {

    private var thread: Thread? = null
    private val messageQueue = LinkedBlockingQueue<Runnable>()

    fun enqueue(runnable: Runnable) {
        if(thread == null) {
            createLooperThread()
        }
        messageQueue.offer(runnable)
    }

    private fun createLooperThread() {
        thread = thread ?: thread {
            try {
                while(true) {
                    val message = messageQueue.take()

                    message.run()
                }
            } catch(e: InterruptedException) { return@thread }
        }
    }

    fun quit() {
        thread?.interrupt()
        thread = null
    }
}