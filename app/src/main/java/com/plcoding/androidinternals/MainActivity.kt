package com.plcoding.androidinternals

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme

class MainActivity : ComponentActivity() {

    private lateinit var myViewModelStore: MyViewModelStore
    private lateinit var myViewModelProvider: MyViewModelProvider
    private lateinit var viewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val lastStore = lastCustomNonConfigurationInstance as? MyViewModelStore
        myViewModelStore = lastStore ?: MyViewModelStore()
        myViewModelProvider = MyViewModelProvider(
            store = myViewModelStore,
            factory = CounterViewModel.Factory(5)
        )
        viewModel = myViewModelProvider.get(CounterViewModel::class.java)

        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(
                        onClick = {
                            viewModel.increment()
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .wrapContentSize()
                    ) {
                        Text("Counter: ${viewModel.counter}")
                    }
                }
            }
        }
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return myViewModelStore
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing) {
            myViewModelStore.clear()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidInternalsTheme {
        Greeting("Android")
    }
}