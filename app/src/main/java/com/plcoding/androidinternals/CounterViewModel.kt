package com.plcoding.androidinternals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CounterViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val counter = savedStateHandle.getStateFlow("counter", 0)

    val greeting = savedStateHandle.get<String>("greeting")

    init {
        println("The greeting is: $greeting")
    }

    fun increment() {
        savedStateHandle.update<Int>("counter") {
            it + 1
        }
    }
}

inline fun <T> SavedStateHandle.update(key: String, update: (T) -> T) {
    synchronized(this) {
        val current = get<T>(key)
        val updated = update(current ?: return)
        set(key, updated)
    }
}