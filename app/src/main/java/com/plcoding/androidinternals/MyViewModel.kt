package com.plcoding.androidinternals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class MyViewModel {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    open fun onCleared() {
        viewModelScope.cancel()
    }
}

class CounterViewModel(
    private val initialCounter: Int
): MyViewModel() {

    var counter by mutableIntStateOf(initialCounter)
        private set

    fun increment() {
        counter++
    }

    class Factory(private val initialCounter: Int): MyViewModelFactory {
        override fun <T : MyViewModel> create(modelClass: Class<T>): T {
            return CounterViewModel(initialCounter) as T
        }
    }
}

class MyViewModelStore {

    private val viewModels = HashMap<String, MyViewModel>()

    fun <T: MyViewModel> put(viewModel: T) {
        viewModels[viewModel::class.java.simpleName] = viewModel
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: MyViewModel> get(clazz: Class<T>): T? {
        return viewModels[clazz.simpleName] as? T
    }

    fun clear() {
        viewModels.values.forEach {
            it.onCleared()
        }
        viewModels.clear()
    }
}

interface MyViewModelFactory {
    fun <T: MyViewModel> create(modelClass: Class<T>): T
}

object DefaultMyViewModelFactory: MyViewModelFactory {
    override fun <T : MyViewModel> create(modelClass: Class<T>): T {
        return modelClass.getDeclaredConstructor().newInstance()
    }
}

class MyViewModelProvider(
    private val store: MyViewModelStore,
    private val factory: MyViewModelFactory = DefaultMyViewModelFactory
) {
    fun <T: MyViewModel> get(modelClass: Class<T>): T {
        val existing = store.get(modelClass)
        if(existing != null && modelClass.isInstance(existing)) {
            return existing
        }

        val viewModel = factory.create(modelClass)
        store.put(viewModel)

        return viewModel
    }
}