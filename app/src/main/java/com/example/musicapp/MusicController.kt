package com.example.musicapp

import android.content.Context
import android.widget.MediaController

class MusicController(c: Context) : MediaController(c) {

    override fun hide() {}

    init {

    }
}




//package com.example.musicapp
//
//import android.content.Context
//import android.widget.*
//import android.widget.MediaController
//
///*
// * Copyright (C) 2006 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
///*
//package android.widget*/
//
////import android.annotation.UnsupportedAppUsage
//import android.content.res.Resources
//import android.graphics.PixelFormat
//import android.media.AudioManager
//import android.os.Build
//import android.util.AttributeSet
//import android.util.Log
//import android.view.Gravity
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import android.view.Window
//import android.view.WindowManager
//import android.view.accessibility.AccessibilityManager
//import android.widget.SeekBar.OnSeekBarChangeListener
//import androidx.core.content.ContextCompat.getSystemService
//
////import com.android.internal.policy.PhoneWindow
//
//import java.util.Formatter
//import java.util.Locale
//
///**
// * A view containing controls for a MediaPlayer. Typically contains the
// * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
// * slider. It takes care of synchronizing the controls with the state
// * of the MediaPlayer.
// *
// *
// * The way to use this class is to instantiate it programmatically.
// * The MediaController will create a default set of controls
// * and put them in a window floating above your application. Specifically,
// * the controls will float above the view specified with setAnchorView().
// * The window will disappear if left idle for three seconds and reappear
// * when the user touches the anchor view.
// *
// *
// * Functions like show() and hide() have no effect when MediaController
// * is created in an xml layout.
// *
// * MediaController will hide and
// * show the buttons according to these rules:
// *
// *  *  The "previous" and "next" buttons are hidden until setPrevNextListeners()
// * has been called
// *  *  The "previous" and "next" buttons are visible but disabled if
// * setPrevNextListeners() was called with null listeners
// *  *  The "rewind" and "fastforward" buttons are shown unless requested
// * otherwise by using the MediaController(Context, boolean) constructor
// * with the boolean set to false
// *
// */
//class com.example.musicapp.MusicController(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
//
////    //@UnsupportedAppUsage
//    private var mPlayer: MediaPlayerControl? = null
////    //@UnsupportedAppUsage
//    private var mContext: Context
////    //@UnsupportedAppUsage
//    private var mAnchor: View? = null
////    //@UnsupportedAppUsage
//    private var mRoot: View? = null
////    //@UnsupportedAppUsage
//    private var mWindowManager: WindowManager? = null
////    //@UnsupportedAppUsage
//    private var mWindow: Window? = null
////    //@UnsupportedAppUsage
//    private var mDecor: View? = null
////    //@UnsupportedAppUsage
//    private var mDecorLayoutParams: WindowManager.LayoutParams? = null
////    //@UnsupportedAppUsage
//    private var mProgress: ProgressBar? = null
////    //@UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.P, trackingBug = 115609023)
//    private var mEndTime: TextView? = null
////    //@UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.P, trackingBug = 115609023)
//    private var mCurrentTime: TextView? = null
////    //@UnsupportedAppUsage
//    var isShowing: Boolean = false
//        private set
//    private var mDragging: Boolean = false
//    private var mUseFastForward: Boolean
//    private var mFromXml: Boolean
//    private var mListenersSet: Boolean = false
//    private var mNextListener: View.OnClickListener? = null
//    private var mPrevListener: View.OnClickListener? = null
//    internal lateinit var mFormatBuilder: StringBuilder
//    internal lateinit var mFormatter: Formatter
////    //@UnsupportedAppUsage
//    private var mPauseButton: ImageButton? = null
////    //@UnsupportedAppUsage
//    private var mFfwdButton: ImageButton? = null
////    //@UnsupportedAppUsage
//    private var mRewButton: ImageButton? = null
////    //@UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.P, trackingBug = 115609023)
//    private var mNextButton: ImageButton? = null
////    //@UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.P, trackingBug = 115609023)
//    private var mPrevButton: ImageButton? = null
//    private var mPlayDescription: CharSequence? = null
//    private var mPauseDescription: CharSequence? = null
//    private var mAccessibilityManager: AccessibilityManager
//
//    init {
//            mRoot = this
//            mContext = context
//            mUseFastForward = true
//            mFromXml = true
//            mAccessibilityManager = mContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
//
//    }
//    // This is called whenever mAnchor's layout bound changes
//    private val mLayoutChangeListener =
//        OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
//            updateFloatingWindowLayout()
//            if (isShowing) {
//                mWindowManager!!.updateViewLayout(mDecor, mDecorLayoutParams)
//            }
//        }
//
//    private val mTouchListener = OnTouchListener { v, event ->
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            if (isShowing) {
//                hide()
//            }
//        }
//        false
//    }
//
//    private val mFadeOut = Runnable { hide() }
//
//    private val mShowProgress = object : Runnable {
//        override fun run() {
//            val pos = setProgress()
//            if (!mDragging && isShowing && mPlayer!!.isPlaying) {
//                postDelayed(this, (1000 - pos % 1000).toLong())
//            }
//        }
//    }
//
//    private val mPauseListener = OnClickListener {
//        doPauseResume()
//        show(sDefaultTimeout)
//    }
//
//    // There are two scenarios that can trigger the seekbar listener to trigger:
//    //
//    // The first is the user using the touchpad to adjust the posititon of the
//    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
//    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
//    // We're setting the field "mDragging" to true for the duration of the dragging
//    // session to avoid jumps in the position in case of ongoing playback.
//    //
//    // The second scenario involves the user operating the scroll ball, in this
//    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
//    // we will simply apply the updated position without suspending regular updates.
//    //@UnsupportedAppUsage
//    private val mSeekListener = object : OnSeekBarChangeListener {
//        override fun onStartTrackingTouch(bar: SeekBar) {
//            show(3600000)
//
//            mDragging = true
//
//            // By removing these pending progress messages we make sure
//            // that a) we won't update the progress while the user adjusts
//            // the seekbar and b) once the user is done dragging the thumb
//            // we will post one of these messages to the queue again and
//            // this ensures that there will be exactly one message queued up.
//            removeCallbacks(mShowProgress)
//        }
//
//        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
//            if (!fromuser) {
//                // We're not interested in programmatically generated changes to
//                // the progress bar's position.
//                return
//            }
//
//            val duration = mPlayer!!.duration.toLong()
//            val newposition = duration * progress / 1000L
//            mPlayer!!.seekTo(newposition.toInt())
//            if (mCurrentTime != null)
//                mCurrentTime!!.text = stringForTime(newposition.toInt())
//        }
//
//        override fun onStopTrackingTouch(bar: SeekBar) {
//            mDragging = false
//            setProgress()
//            updatePausePlay()
//            show(sDefaultTimeout)
//
//            // Ensure that progress is properly updated in the future,
//            // the call to show() does not guarantee this because it is a
//            // no-op if we are already showing.
//            post(mShowProgress)
//        }
//    }
//
//    //@UnsupportedAppUsage
//    private val mRewListener = OnClickListener {
//        var pos = mPlayer!!.currentPosition
//        pos -= 5000 // milliseconds
//        mPlayer!!.seekTo(pos)
//        setProgress()
//
//        show(sDefaultTimeout)
//    }
//
//    //@UnsupportedAppUsage
//    private val mFfwdListener = OnClickListener {
//        var pos = mPlayer!!.currentPosition
//        pos += 15000 // milliseconds
//        mPlayer!!.seekTo(pos)
//        setProgress()
//
//        show(sDefaultTimeout)
//    }
//
//    public override fun onFinishInflate() {
//        if (mRoot != null)
//            initControllerView(mRoot!!)
//        super.onFinishInflate() // TODO
//    }
//
//    private fun initFloatingWindow() {
//        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        mWindow = PhoneWindow(mContext)
//        mWindow!!.setWindowManager(mWindowManager, null, null)
//        mWindow!!.requestFeature(Window.FEATURE_NO_TITLE)
//        mDecor = mWindow!!.decorView
//        mDecor!!.setOnTouchListener(mTouchListener)
//        mWindow!!.setContentView(this)
//        mWindow!!.setBackgroundDrawableResource(android.R.color.transparent)
//
//        // While the media controller is up, the volume control keys should
//        // affect the media stream type
//        mWindow!!.volumeControlStream = AudioManager.STREAM_MUSIC
//
//        isFocusable = true
//        isFocusableInTouchMode = true
//        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
//        requestFocus()
//    }
//
//    // Allocate and initialize the static parts of mDecorLayoutParams. Must
//    // also call updateFloatingWindowLayout() to fill in the dynamic parts
//    // (y and width) before mDecorLayoutParams can be used.
//    private fun initFloatingWindowLayout() {
//        mDecorLayoutParams = WindowManager.LayoutParams()
//        val p = mDecorLayoutParams
//        p!!.gravity = Gravity.TOP or Gravity.LEFT
//        p.height = FrameLayout.LayoutParams.WRAP_CONTENT
//        p.x = 0
//        p.format = PixelFormat.TRANSLUCENT
//        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
//        p.flags = p.flags or (WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
//                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                or WindowManager.LayoutParams.FLAG_SPLIT_TOUCH)
//        p.token = null
//        p.windowAnimations = 0 // android.R.style.DropDownAnimationDown;
//    }
//
//    // Update the dynamic parts of mDecorLayoutParams
//    // Must be called with mAnchor != NULL.
//    private fun updateFloatingWindowLayout() {
//        val anchorPos = IntArray(2)
//        mAnchor!!.getLocationOnScreen(anchorPos)
//
//        // we need to know the size of the controller so we can properly position it
//        // within its space
//        mDecor!!.measure(
//            View.MeasureSpec.makeMeasureSpec(mAnchor!!.width, View.MeasureSpec.AT_MOST),
//            View.MeasureSpec.makeMeasureSpec(mAnchor!!.height, View.MeasureSpec.AT_MOST)
//        )
//
//        val p = mDecorLayoutParams
//        p!!.width = mAnchor!!.width
//        p.x = anchorPos[0] + (mAnchor!!.width - p.width) / 2
//        p.y = anchorPos[1] + mAnchor!!.height - mDecor!!.measuredHeight
//    }
//
//    fun setMediaPlayer(player: MediaPlayerControl) {
//        mPlayer = player
//        updatePausePlay()
//    }
//
//    /**
//     * Set the view that acts as the anchor for the control view.
//     * This can for example be a VideoView, or your Activity's main view.
//     * When VideoView calls this method, it will use the VideoView's parent
//     * as the anchor.
//     * @param view The view to which to anchor the controller when it is visible.
//     */
//    fun setAnchorView(view: View) {
//        if (mAnchor != null) {
//            mAnchor!!.removeOnLayoutChangeListener(mLayoutChangeListener)
//        }
//        mAnchor = view
//        if (mAnchor != null) {
//            mAnchor!!.addOnLayoutChangeListener(mLayoutChangeListener)
//        }
//
//        val frameParams = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
//
//        removeAllViews()
//        val v = makeControllerView()
//        addView(v, frameParams)
//    }
//
//    /**
//     * Create the view that holds the widgets that control playback.
//     * Derived classes can override this to create their own.
//     * @return The controller view.
//     * @hide This doesn't work as advertised
//     */
//    protected fun makeControllerView(): View {
//        val inflate = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        mRoot = inflate.inflate(com.android.internal.R.layout.media_controller, null)
//
//        initControllerView(mRoot!!)
//
//        return mRoot
//    }
//
//    private fun initControllerView(v: View) {
//        val res = mContext.resources
//        mPlayDescription = res
//            .getText(com.android.internal.R.string.lockscreen_transport_play_description)
//        mPauseDescription = res
//            .getText(com.android.internal.R.string.lockscreen_transport_pause_description)
//        mPauseButton = v.findViewById(com.android.internal.R.id.pause)
//        if (mPauseButton != null) {
//            mPauseButton!!.requestFocus()
//            mPauseButton!!.setOnClickListener(mPauseListener)
//        }
//
//        mFfwdButton = v.findViewById(com.android.internal.R.id.ffwd)
//        if (mFfwdButton != null) {
//            mFfwdButton!!.setOnClickListener(mFfwdListener)
//            if (!mFromXml) {
//                mFfwdButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
//            }
//        }
//
//        mRewButton = v.findViewById(com.android.internal.R.id.rew)
//        if (mRewButton != null) {
//            mRewButton!!.setOnClickListener(mRewListener)
//            if (!mFromXml) {
//                mRewButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
//            }
//        }
//
//        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
//        mNextButton = v.findViewById(com.android.internal.R.id.next)
//        if (mNextButton != null && !mFromXml && !mListenersSet) {
//            mNextButton!!.visibility = View.GONE
//        }
//        mPrevButton = v.findViewById(com.android.internal.R.id.prev)
//        if (mPrevButton != null && !mFromXml && !mListenersSet) {
//            mPrevButton!!.visibility = View.GONE
//        }
//
//        mProgress = v.findViewById(com.android.internal.R.id.mediacontroller_progress)
//        if (mProgress != null) {
//            if (mProgress is SeekBar) {
//                val seeker = mProgress as SeekBar?
//                seeker!!.setOnSeekBarChangeListener(mSeekListener)
//            }
//            mProgress!!.max = 1000
//        }
//
//        mEndTime = v.findViewById(com.android.internal.R.id.time)
//        mCurrentTime = v.findViewById(com.android.internal.R.id.time_current)
//        mFormatBuilder = StringBuilder()
//        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
//
//        installPrevNextListeners()
//    }
//
//    /**
//     * Disable pause or seek buttons if the stream cannot be paused or seeked.
//     * This requires the control interface to be a MediaPlayerControlExt
//     */
//    private fun disableUnsupportedButtons() {
//        try {
//            if (mPauseButton != null && !mPlayer!!.canPause()) {
//                mPauseButton!!.isEnabled = false
//            }
//            if (mRewButton != null && !mPlayer!!.canSeekBackward()) {
//                mRewButton!!.isEnabled = false
//            }
//            if (mFfwdButton != null && !mPlayer!!.canSeekForward()) {
//                mFfwdButton!!.isEnabled = false
//            }
//            // TODO What we really should do is add a canSeek to the MediaPlayerControl interface;
//            // this scheme can break the case when applications want to allow seek through the
//            // progress bar but disable forward/backward buttons.
//            //
//            // However, currently the flags SEEK_BACKWARD_AVAILABLE, SEEK_FORWARD_AVAILABLE,
//            // and SEEK_AVAILABLE are all (un)set together; as such the aforementioned issue
//            // shouldn't arise in existing applications.
//            if (mProgress != null && !mPlayer!!.canSeekBackward() && !mPlayer!!.canSeekForward()) {
//                mProgress!!.isEnabled = false
//            }
//        } catch (ex: IncompatibleClassChangeError) {
//            // We were given an old version of the interface, that doesn't have
//            // the canPause/canSeekXYZ methods. This is OK, it just means we
//            // assume the media can be paused and seeked, and so we don't disable
//            // the buttons.
//        }
//
//    }
//
//    /**
//     * Show the controller on screen. It will go away
//     * automatically after 'timeout' milliseconds of inactivity.
//     * @param timeout The timeout in milliseconds. Use 0 to show
//     * the controller until hide() is called.
//     */
//    @JvmOverloads
//    fun show(timeout: Int = sDefaultTimeout) {
//        if (!isShowing && mAnchor != null) {
//            setProgress()
//            if (mPauseButton != null) {
//                mPauseButton!!.requestFocus()
//            }
//            disableUnsupportedButtons()
//            updateFloatingWindowLayout()
//            mWindowManager!!.addView(mDecor, mDecorLayoutParams)
//            isShowing = true
//        }
//        updatePausePlay()
//
//        // cause the progress bar to be updated even if mShowing
//        // was already true.  This happens, for example, if we're
//        // paused with the progress bar showing the user hits play.
//        post(mShowProgress)
//
//        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled) {
//            removeCallbacks(mFadeOut)
//            postDelayed(mFadeOut, timeout.toLong())
//        }
//    }
//
//    /**
//     * Remove the controller from the screen.
//     */
//    fun hide() {
//        if (mAnchor == null)
//            return
//
//        if (isShowing) {
//            try {
//                removeCallbacks(mShowProgress)
//                mWindowManager!!.removeView(mDecor)
//            } catch (ex: IllegalArgumentException) {
//                Log.w("MediaController", "already removed")
//            }
//
//            isShowing = false
//        }
//    }
//
//    private fun stringForTime(timeMs: Int): String {
//        val totalSeconds = timeMs / 1000
//
//        val seconds = totalSeconds % 60
//        val minutes = totalSeconds / 60 % 60
//        val hours = totalSeconds / 3600
//
//        mFormatBuilder.setLength(0)
//        return if (hours > 0) {
//            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
//        } else {
//            mFormatter.format("%02d:%02d", minutes, seconds).toString()
//        }
//    }
//
//    private fun setProgress(): Int {
//        if (mPlayer == null || mDragging) {
//            return 0
//        }
//        val position = mPlayer!!.currentPosition
//        val duration = mPlayer!!.duration
//        if (mProgress != null) {
//            if (duration > 0) {
//                // use long to avoid overflow
//                val pos = 1000L * position / duration
//                mProgress!!.progress = pos.toInt()
//            }
//            val percent = mPlayer!!.bufferPercentage
//            mProgress!!.secondaryProgress = percent * 10
//        }
//
//        if (mEndTime != null)
//            mEndTime!!.text = stringForTime(duration)
//        if (mCurrentTime != null)
//            mCurrentTime!!.text = stringForTime(position)
//
//        return position
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> show(0) // show until hide is called
//            MotionEvent.ACTION_UP -> show(sDefaultTimeout) // start timeout
//            MotionEvent.ACTION_CANCEL -> hide()
//            else -> {
//            }
//        }
//        return true
//    }
//
//    override fun onTrackballEvent(ev: MotionEvent): Boolean {
//        show(sDefaultTimeout)
//        return false
//    }
//
//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        val keyCode = event.keyCode
//        val uniqueDown = event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN
//        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
//            || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//            || keyCode == KeyEvent.KEYCODE_SPACE
//        ) {
//            if (uniqueDown) {
//                doPauseResume()
//                show(sDefaultTimeout)
//                if (mPauseButton != null) {
//                    mPauseButton!!.requestFocus()
//                }
//            }
//            return true
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
//            if (uniqueDown && !mPlayer!!.isPlaying) {
//                mPlayer!!.start()
//                updatePausePlay()
//                show(sDefaultTimeout)
//            }
//            return true
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//            if (uniqueDown && mPlayer!!.isPlaying) {
//                mPlayer!!.pause()
//                updatePausePlay()
//                show(sDefaultTimeout)
//            }
//            return true
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//            || keyCode == KeyEvent.KEYCODE_VOLUME_UP
//            || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
//            || keyCode == KeyEvent.KEYCODE_CAMERA
//        ) {
//            // don't show the controls for volume adjustment
//            return super.dispatchKeyEvent(event)
//        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
//            if (uniqueDown) {
//                hide()
//            }
//            return true
//        }
//
//        show(sDefaultTimeout)
//        return super.dispatchKeyEvent(event)
//    }
//
//    //@UnsupportedAppUsage
//    private fun updatePausePlay() {
//        if (mRoot == null || mPauseButton == null)
//            return
//
//        if (mPlayer!!.isPlaying) {
//            mPauseButton!!.setImageResource(com.android.internal.R.drawable.ic_media_pause)
//            mPauseButton!!.contentDescription = mPauseDescription
//        } else {
//            mPauseButton!!.setImageResource(com.android.internal.R.drawable.ic_media_play)
//            mPauseButton!!.contentDescription = mPlayDescription
//        }
//    }
//
//    private fun doPauseResume() {
//        if (mPlayer!!.isPlaying) {
//            mPlayer!!.pause()
//        } else {
//            mPlayer!!.start()
//        }
//        updatePausePlay()
//    }
//
//    override fun setEnabled(enabled: Boolean) {
//        if (mPauseButton != null) {
//            mPauseButton!!.isEnabled = enabled
//        }
//        if (mFfwdButton != null) {
//            mFfwdButton!!.isEnabled = enabled
//        }
//        if (mRewButton != null) {
//            mRewButton!!.isEnabled = enabled
//        }
//        if (mNextButton != null) {
//            mNextButton!!.isEnabled = enabled && mNextListener != null
//        }
//        if (mPrevButton != null) {
//            mPrevButton!!.isEnabled = enabled && mPrevListener != null
//        }
//        if (mProgress != null) {
//            mProgress!!.isEnabled = enabled
//        }
//        disableUnsupportedButtons()
//        super.setEnabled(enabled)
//    }
//
//    override fun getAccessibilityClassName(): CharSequence {
//        return MediaController::class.java.name
//    }
//
//    private fun installPrevNextListeners() {
//        if (mNextButton != null) {
//            mNextButton!!.setOnClickListener(mNextListener)
//            mNextButton!!.isEnabled = mNextListener != null
//        }
//
//        if (mPrevButton != null) {
//            mPrevButton!!.setOnClickListener(mPrevListener)
//            mPrevButton!!.isEnabled = mPrevListener != null
//        }
//    }
//
//    fun setPrevNextListeners(next: View.OnClickListener, prev: View.OnClickListener) {
//        mNextListener = next
//        mPrevListener = prev
//        mListenersSet = true
//
//        if (mRoot != null) {
//            installPrevNextListeners()
//
//            if (mNextButton != null && !mFromXml) {
//                mNextButton!!.visibility = View.VISIBLE
//            }
//            if (mPrevButton != null && !mFromXml) {
//                mPrevButton!!.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    interface MediaPlayerControl {
//        val duration: Int
//        val currentPosition: Int
//        val isPlaying: Boolean
//        val bufferPercentage: Int
//
//        /**
//         * Get the audio session id for the player used by this VideoView. This can be used to
//         * apply audio effects to the audio track of a video.
//         * @return The audio session, or 0 if there was an error.
//         */
//        val audioSessionId: Int
//
//        fun start()
//        fun pause()
//        fun seekTo(pos: Int)
//        fun canPause(): Boolean
//        fun canSeekBackward(): Boolean
//        fun canSeekForward(): Boolean
//    }
//
//    companion object {
//        private val sDefaultTimeout = 3000
//    }
//}
///**
// * Show the controller on screen. It will go away
// * automatically after 3 seconds of inactivity.
// */
