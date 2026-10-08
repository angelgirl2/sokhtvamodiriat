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

    private val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u26FF\\u2700-\\u27BF\\uFE00-\\uFE0F\\p{So}]")

    fun show(message: String) {
        val cleanMessage = message.replace(emojiRegex, "").trim()
        _messageFlow.tryEmit(cleanMessage)
    }
}
