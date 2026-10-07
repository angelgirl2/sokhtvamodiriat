package com.angelgirlbrand.modiratsokhtandestelam.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centrally manages custom theme-matching application notifications
 * to replace default white-background system Toasts.
 */
object AppToast {
    private val _messageFlow = MutableSharedFlow<String>(extraBufferCapacity = 15)
    val messageFlow = _messageFlow.asSharedFlow()

    fun show(message: String) {
        _messageFlow.tryEmit(message)
    }
}
