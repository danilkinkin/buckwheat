package com.danilkinkin.buckwheat.util

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

@Composable
fun <T> observeLiveData(data: MutableLiveData<T>, callback: Observer<T>) {
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner) {
        data.observeForever(callback)

        onDispose {
            data.removeObserver(callback)
        }
    }
}

@Composable
fun <T> observeLiveData(data: LiveData<T>, callback: Observer<T>) {
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner) {
        data.observeForever(callback)

        onDispose {
            data.removeObserver(callback)
        }
    }
}