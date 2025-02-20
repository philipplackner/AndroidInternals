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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme
import kotlinx.serialization.Serializable

@Serializable
data object ScreenA

@Serializable
data object ScreenB

@Serializable
data object ScreenC

@Serializable
data object ScreenBAndCGraph

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
                            Column {
                                CounterUi(
                                    screenName = "Screen A",
                                    counter = viewModel.counter,
                                    onIncrementClick = viewModel::increment,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        navController.navigate(ScreenBAndCGraph)
                                    }
                                ) {
                                    Text("Go to screen B")
                                }
                            }
                        }

                        navigation<ScreenBAndCGraph>(
                            startDestination = ScreenB
                        ) {
                            composable<ScreenB> {
                                val viewModel = it.sharedViewModel<CounterViewModel>(navController)
                                Column {
                                    CounterUi(
                                        screenName = "Screen B",
                                        counter = viewModel.counter,
                                        onIncrementClick = viewModel::increment,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            navController.navigate(ScreenC)
                                        }
                                    ) {
                                        Text("Go to screen C")
                                    }
                                }
                            }

                            composable<ScreenC> {
                                val viewModel = it.sharedViewModel<CounterViewModel>(navController)
                                CounterUi(
                                    screenName = "Screen C",
                                    counter = viewModel.counter,
                                    onIncrementClick = viewModel::increment
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
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(viewModelStoreOwner = parentEntry)
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