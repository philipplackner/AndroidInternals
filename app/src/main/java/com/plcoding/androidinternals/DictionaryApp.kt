package com.plcoding.androidinternals

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DictionaryApp: Application() {

    val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}