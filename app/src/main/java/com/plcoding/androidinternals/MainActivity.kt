package com.plcoding.androidinternals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var greeting by remember {
                        mutableStateOf<String?>(null)
                    }
                    val launcher = rememberLauncherForActivityResult(
                        contract = GreetMeContract
                    ) { result ->
                        greeting = result
                    }
                    Button(
                        onClick = {
                            launcher.launch(Unit)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .wrapContentSize()
                    ) {
                        Text(text = greeting ?: "Launch Activity for result")
                    }
                }
            }
        }
    }
}

object GreetMeContract: ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent("com.plcoding.ACTION_GREET_ME").apply {
            `package` = "com.plcoding.androidinternals2"
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return intent?.getStringExtra("greeting")
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