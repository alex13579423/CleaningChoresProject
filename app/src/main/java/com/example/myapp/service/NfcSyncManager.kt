package com.example.myapp.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NfcSyncManager {
    private val _syncSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val syncSuccess = _syncSuccess.asSharedFlow()

    fun notifySuccess() {
        _syncSuccess.tryEmit(Unit)
    }
}
