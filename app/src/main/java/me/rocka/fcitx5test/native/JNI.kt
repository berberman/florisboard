package me.rocka.fcitx5test.native

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber


object JNI : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private val eventFlow_ =
        MutableSharedFlow<FcitxEvent<*>>(extraBufferCapacity = 15, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val eventFlow = eventFlow_.asSharedFlow()

    init {
        System.loadLibrary("native-lib")
    }

    external fun startupFcitx(
        appData: String,
        appLib: String,
        extData: String,
        appDataLibime: String
    ): Int

    external fun sendKeyToFcitx(key: String)

    external fun sendKeyToFcitx(c: Char)

    external fun selectCandidate(idx: Int)

    /**
     * Called from native-lib
     */
    @Suppress("unused")
    private fun handleFcitxEvent(type: Int, vararg params: Any) {
        launch {
            eventFlow_.emit(FcitxEvent.create(type, params.asList()))
            Timber.d("Fcitx Event: type ${type}, args ${params.run { "[$size]" + joinToString(",") }}")
        }
    }

}
