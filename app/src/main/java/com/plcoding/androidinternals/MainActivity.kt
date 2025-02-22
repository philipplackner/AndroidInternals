package com.plcoding.androidinternals

import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme

class MainActivity : ComponentActivity() {

    private val screenOffReceiver = ScreenOffReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ContextCompat.registerReceiver(
            applicationContext,
            screenOffReceiver,
            IntentFilter("android.intent.action.SCREEN_OFF"),
            ContextCompat.RECEIVER_EXPORTED
        )

        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(
                        onClick = {
                            Intent("com.plcoding.ACTION_GREET_VIA_BROADCAST").also {
                                it.`package` = "com.plcoding.androidinternals2"
                                sendBroadcast(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .wrapContentSize()
                    ) {
                        Text("Send broadcast")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }
}