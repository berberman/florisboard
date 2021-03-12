package dev.patrickgold.florisboard.ime.clip

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.patrickgold.florisboard.BuildConfig
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.ime.core.FlorisBoard
import dev.patrickgold.florisboard.ime.core.InputKeyEvent
import dev.patrickgold.florisboard.ime.core.InputView
import dev.patrickgold.florisboard.ime.text.key.KeyCode
import dev.patrickgold.florisboard.ime.text.key.KeyData
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.pow

/**
 * Handles the clipboard view and allows for communication between UI and logic.
 */
class ClipboardInputManager private constructor() : CoroutineScope by MainScope(),
    FlorisBoard.EventListener{

    private var dataSet: ArrayDeque<FlorisClipboardManager.TimedClipData>? = null
    private val florisboard = FlorisBoard.getInstance()
    private var repeatedKeyPressHandler: Handler? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: ClipboardHistoryItemAdapter? = null

    companion object {
        private var instance: ClipboardInputManager? = null

        @Synchronized
        fun getInstance(): ClipboardInputManager {
            if (instance == null) {
                instance = ClipboardInputManager()
            }
            return instance!!
        }
    }

    init {
        florisboard.addEventListener(this)
    }

    override fun onCreateInputView() {
        super.onCreateInputView()
        repeatedKeyPressHandler = Handler(florisboard.context.mainLooper)
    }

    /**
     * Called when a new input view has been registered. Used to initialize all media-relevant
     * views and layouts.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onRegisterInputView(inputView: InputView) {
        Timber.i("onRegisterInputView(inputView)")

        launch(Dispatchers.Default) {

            inputView.findViewById<Button>(R.id.back_to_keyboard_button)
                .setOnTouchListener { view, event -> onButtonPressEvent(view, event) }

            inputView.findViewById<ImageButton>(R.id.clear_clipboard_history)
                .setOnTouchListener { view, event -> onButtonPressEvent(view, event) }

            recyclerView = inputView.findViewById(R.id.clipboard_history_items)

            if (BuildConfig.DEBUG && adapter == null) {
                error("initClipboard() not called")
            }

            recyclerView!!.adapter = adapter
            val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            recyclerView!!.layoutManager = manager
        }
    }

    /**
     * Clean-up of resources and stopping all coroutines.
     */
    override fun onDestroy() {
        Timber.i("onDestroy()")

        cancel()
        instance = null
    }


    fun getClipboardHistoryView() : ClipboardHistoryView{
        return FlorisBoard.getInstance().inputView?.mainViewFlipper?.getChildAt(2) as ClipboardHistoryView
    }

    fun getPositionOfView(view: View): Int {
        return recyclerView?.getChildLayoutPosition(view)!!
    }

    fun notifyItemInserted(position: Int) = adapter?.notifyItemInserted(position)

    fun notifyItemRemoved(position: Int) = adapter?.notifyItemRemoved(position)

    fun notifyItemRangeRemoved(start: Int, numberOfItems: Int) = adapter?.notifyItemRangeRemoved(start, numberOfItems)

    fun notifyItemMoved(from: Int, to: Int) = adapter?.notifyItemMoved(from, to)

    /**
     * Handles clicks on the back to keyboard button.
     */
    private fun onButtonPressEvent(view: View, event: MotionEvent?): Boolean {
        Timber.d("onBottomButtonEvent")

        event ?: return false
        val data = when (view.id) {
            R.id.back_to_keyboard_button -> KeyData(code = KeyCode.SWITCH_TO_TEXT_CONTEXT)
            R.id.clear_clipboard_history -> KeyData(code = KeyCode.CLEAR_CLIPBOARD_HISTORY)
            else -> null
        }!!
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                florisboard.keyPressVibrate()
                florisboard.keyPressSound(data)
                florisboard.textInputManager.inputEventDispatcher.send(InputKeyEvent.down(data))
            }
            MotionEvent.ACTION_UP -> {
                florisboard.textInputManager.inputEventDispatcher.send(InputKeyEvent.up(data))
            }
            MotionEvent.ACTION_CANCEL -> {
                florisboard.textInputManager.inputEventDispatcher.send(InputKeyEvent.cancel(data))
            }
        }

        // MUST return false here so the background selector for showing a transparent bg works
        return false
    }

    /**
     * [recyclerView] will be linked to [dataSet] when initialized.
     *
     * @param dataSet the data set to link to
     */
    fun initClipboard(dataSet: ArrayDeque<FlorisClipboardManager.TimedClipData>, pins: ArrayDeque<ClipData>) {
        this.adapter =  ClipboardHistoryItemAdapter(dataSet = dataSet, pins= pins)
        this.dataSet = dataSet
    }

    /**
     * Plays an animation of all items moving off the the clipboard from the top.
     *
     * @param start The index to start at (to ignore pins)
     * @param size The size of the clipboard
     * @return The time in millis till the last animation will complete.
     */
    fun clearClipboardWithAnimation(start: Int, size: Int): Long {
        // list of views to animate
        val views = arrayListOf<View>()
        for(i in 0 until size){
            recyclerView?.findViewHolderForLayoutPosition(i + start)?.let {
                views.add(it.itemView)
            }
        }

        // animate the views
        var delay = 1L
        for (view in views) {
            delay += (10 * delay.toDouble().pow(0.1)).toLong()
            val an = view.animate().translationX(1500f)
            an.startDelay = delay
            an.duration = 250
        }

        // a little while later we reset the views so they can be reused.
        Handler(Looper.getMainLooper()).postDelayed({
            for (view in views) {
                view.translationX = 0f
            }
        }, 450 + delay)

        return 280 + delay
    }

    fun notifyItemChanged(i: Int) = adapter?.notifyItemChanged(i)

}
