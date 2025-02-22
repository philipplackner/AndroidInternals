@file:OptIn(ExperimentalFoundationApi::class)

package com.plcoding.androidinternals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MusicControlsUi(
    controller: MusicServiceController,
    modifier: Modifier = Modifier
) {
    val isConnected by controller.isConnected.collectAsStateWithLifecycle()
    val currentSong by controller.currentSong.collectAsStateWithLifecycle()
    val green = Color(0xFF099A00)
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if(isConnected) {
            Text(
                text = "Connected",
                color = green,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        } else {
            Text(
                text = "Disconnected",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = controller::previous
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }
                Text(
                    text = currentSong,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = controller::next
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }
            }
            Button(
                onClick = {
                    if(isConnected) {
                        controller.unbind()
                    } else {
                        controller.bind()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isConnected) {
                        Color.Red
                    } else {
                        green
                    }
                )
            ) {
                if(isConnected) {
                    Text(text = "Disconnect",)
                } else {
                    Text(text = "Connect")
                }
            }
        }
    }
}