package com.plcoding.androidinternals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme
import kotlinx.serialization.Serializable

@Serializable
data object ScreenA

@Serializable
data class ScreenB(val greeting: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        startDestination = ScreenA
                    ) {
                        composable<ScreenA> {
                            val viewModel = viewModel<CounterViewModel>()
                            val counter by viewModel.counter.collectAsStateWithLifecycle()

                            val screenBCounter by it.savedStateHandle.getStateFlow(
                                "screen_b_counter",
                                0
                            ).collectAsStateWithLifecycle()

                            LaunchedEffect(true) {
                                println("The counter on screen B was $screenBCounter")
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CounterUi(
                                    screenName = "Screen A",
                                    counter = counter,
                                    onIncrementClick = viewModel::increment,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        navController.navigate(ScreenB("Hello world!"))
                                    },
                                ) {
                                    Text("Go to screen B")
                                }
                            }
                        }

                        composable<ScreenB> {
                            val viewModel = viewModel<CounterViewModel>()
                            val counter by viewModel.counter.collectAsStateWithLifecycle()

                            LaunchedEffect(counter) {
                                navController
                                    .previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("screen_b_counter", counter)
                            }

                            Column {
                                CounterUi(
                                    screenName = "Screen B",
                                    counter = counter,
                                    onIncrementClick = {
                                        viewModel.increment()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CounterUi(
    screenName: String,
    counter: Int,
    onIncrementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = screenName,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
        Button(onClick = onIncrementClick) {
            Text("Counter: $counter")
        }
    }
}