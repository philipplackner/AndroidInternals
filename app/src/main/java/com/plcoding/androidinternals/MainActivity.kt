package com.plcoding.androidinternals

import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val myLooper = MyLooper()

        repeat(5) {
            myLooper.enqueue(sampleRunnable(it))
        }
        lifecycleScope.launch {
            delay(10000)
            myLooper.enqueue(sampleRunnable(5))
        }

        val viewModel = MyViewModel()
        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val counter by viewModel.counter.collectAsState(
                        context = Dispatchers.Main.immediate
                    )

                    Button(
                        onClick = {
                            viewModel.increment()
                            println("Counter: $counter")
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    ) {
                        Text("Counter: $counter")
                    }
                }
            }
        }
    }

    private fun sampleRunnable(index: Int): Runnable {
        return Runnable {
            println("Runnable $index started.")
            Thread.sleep(1000L)
            println("Runnable $index finished.")
        }
    }
}

class MyViewModel {

    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    fun increment() {
        _counter.update { it + 1 }
    }
}