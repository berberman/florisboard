package me.rocka.fcitx5test.native

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object JNI : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val eventFlow_ =
        MutableSharedFlow<FcitxEvent<*>>(extraBufferCapacity = 15, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Subscribe this flow to receive event sent from fcitx
     */
    val eventFlow = eventFlow_.asSharedFlow()

    init {
        System.loadLibrary("native-lib")
    }

    private external fun startupFcitx(appData: String, appLib: String, extData: String): Int

    /**
     * Startup fcitx event loop
     */
    fun startupFcitx(context: Context) = with(context) {
        launch {
            // copy our assets
            // TODO: implement a manager
            copyFileOrDir("fcitx5")
            startupFcitx(
                applicationInfo.dataDir,
                applicationInfo.nativeLibraryDir,
                getExternalFilesDir(null)!!.absolutePath
            )
        }
    }

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
            Timber.d("handleFcitxEvent fcitx event: type ${type}, args ${params.run { "[$size]" + joinToString(",") }}")
        }
    }

}
