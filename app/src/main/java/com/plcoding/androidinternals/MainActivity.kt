package com.plcoding.androidinternals

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.lifecycleScope
import com.plcoding.androidinternals.ui.theme.AndroidInternalsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            writeToExternalStorage()
        }

        setContent {
            AndroidInternalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var bmp by remember {
                            mutableStateOf<Bitmap?>(null)
                        }
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri ->
                            uri?.let { uri ->
                                lifecycleScope.launch {
                                    bmp = readUriAsBitmap(uri)
                                }
                            }
                        }
                        Button(
                            onClick = {
                                launcher.launch(arrayOf("image/*"))
                            }
                        ) {
                            Text("Pick photo")
                        }
                        bmp?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun readUriAsBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val bytes = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return@withContext null

        BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size
        )
    }

    private suspend fun writeToInternalStorage() = withContext(Dispatchers.IO) {
        val file = File(filesDir, "hello.txt")
        FileOutputStream(file).use { outputStream ->
            outputStream.write("Hello world!".encodeToByteArray())
        }
    }

    private suspend fun writeToExternalStorage() = withContext(Dispatchers.IO) {
        val directory = getExternalFilesDir(null)
        val file = File(directory, "hello.txt")
        FileOutputStream(file).use { outputStream ->
            outputStream.write("Hello world!".encodeToByteArray())
        }
    }
}