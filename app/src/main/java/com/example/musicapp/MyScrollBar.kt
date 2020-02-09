package com.example.musicapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColor
import java.lang.Float.max
import java.lang.Float.min
import androidx.core.graphics.minus
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
import android.util.DisplayMetrics


class MyScrollBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // use cases
    // normal: move does absolute positioning
    // relative: down->...->up-at-close-position does absolute, down->move(drag) does relative positioning (advance at a fixed rate proportional to cursor movement)

    var direction: Int; // 0 horizontal, 1 vertical
    var scrollMode: Int // 0 normal, 1 relative
    var scrollRate: Float // for relative mode only. How many units to move per inch
    var thumbWidth: Float;
    var thumbStyle: Int // 0 normal, 1 progress
    var mPosition: Float = 0.0f;
    val positionMax: Float = 1.0f;
    var boundingColor: Color;
    var thumbColor: Color;
    var onChanged: ((position: Float)->Unit)? = null

    private var w: Int = 0
    private var h: Int = 0

    private var downPoint = PointF(0.0f, 0.0f)
    private var prevPosition: Float
    private var maybeClick: Boolean = false
    private val MOVE_THRESHOLD = 10.0f

    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val mDisplayMetric : DisplayMetrics = context.resources.displayMetrics
    var DPI: Float

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MyScrollBar,
            0, 0).apply {

            try {
                direction = getInteger(R.styleable.MyScrollBar_direction, 0)
                scrollMode = getInteger(R.styleable.MyScrollBar_scrollMode, 0)
                scrollRate = getFloat(R.styleable.MyScrollBar_scrollRate, 0.1f)
                thumbStyle = getInteger(R.styleable.MyScrollBar_thumbStyle, 0)
                thumbWidth = getFloat(R.styleable.MyScrollBar_thumbWidth, if (thumbStyle == 0) 0.1f else 0.0f)
//                setMax(getFloat(R.styleable.MyScrollBar_positionMax, 1.0f))
                setPercent(getFloat(R.styleable.MyScrollBar_position, 0.0f) / positionMax)
                boundingColor = getColor(R.styleable.MyScrollBar_boundingColor, Color.LTGRAY).toColor()
                thumbColor = getColor(R.styleable.MyScrollBar_thumbColor, Color.parseColor("#7dbeff")).toColor()


                DPI = if (isVert()) mDisplayMetric.ydpi else mDisplayMetric.xdpi
                prevPosition = mPosition

            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {

            val l = (if (isVert()) h else w) // long
            val s = (if (isVert()) w else h) // short

            val thumbSize = thumbWidth * l
            val thumbHalf = thumbSize / 2

            val boundingSize = l - thumbSize
            val boundingStart = thumbHalf
            val boundingEnd = l - thumbHalf

            // TODO progress bar style thumb (start at 0 always)
            val thumbCenter = getPercent() * (boundingSize) + boundingStart
            val thumbStart = if (thumbStyle == 0) thumbCenter - thumbHalf else 0.0f
            val thumbEnd = thumbCenter + thumbHalf;

//            //val centerSize = thumbSize * 0.01f
//            val centerSize = max(2.0f, thumbSize * 0.01f)
            val centerSize = 2.0f
            val thumbCenterStart = thumbCenter - centerSize
            val thumbCenterEnd = thumbCenter + centerSize

            var r : Rect;

            // bounding box
            rectPaint.color = boundingColor.toArgb()
            r = createRect(boundingStart.toInt(), 0, boundingEnd.toInt(), s, isVert())
//            r = createRect(0, 0, l, s, isVert())
            drawRect(r, rectPaint)

            //thumb
            rectPaint.color = thumbColor.toArgb()
            r = createRect(thumbStart.toInt(), 0, thumbEnd.toInt(), s, isVert())
            drawRect(r, rectPaint)

            //thumb center
            rectPaint.color = Color.BLACK
            r = createRect(thumbCenterStart.toInt(), 0, thumbCenterEnd.toInt(), s, isVert())
            drawRect(r, rectPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun createRect(left: Int, top: Int, right: Int, bottom: Int, vertical: Boolean=false) : Rect {
        return Rect(if (vertical) top else left, if (vertical) left else top, if (vertical) bottom else right, if (vertical) right else bottom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.apply {
            val percent = (if (isVert()) y else x) / (if (isVert()) h else w).toFloat()
            var deltaDown = (PointF(x, y) - downPoint)
            var dif = deltaDown.length()

            if (maybeClick && (dif > MOVE_THRESHOLD)) {
                maybeClick = false
                downPoint.set(x, y)
                // TODO consolidate with above
                deltaDown = (PointF(x, y) - downPoint) // or 0, 0
                dif = deltaDown.length()
            }

            if (scrollMode == 0) {
                // any event triggers change for normal mode
                setPercent(percent)
                onChanged?.invoke(percent)
            }

            if (MotionEvent.ACTION_DOWN == action) {
                maybeClick = true
                prevPosition = mPosition
                downPoint.set(x, y)
            }
            else if (MotionEvent.ACTION_UP == action) {
                if (maybeClick) {
                    // click always does an absolute move
                    setPercent(percent)
                    onChanged?.invoke(percent)
                    // its a click
                    performClick()
                }
            }
            else if(MotionEvent.ACTION_MOVE == action) {
                if (scrollMode == 1 && !maybeClick) {
                    // convert delta since down into inches of travel
                    val deltaPX = if (isVert()) deltaDown.y else deltaDown.x
                    val deltaInches = deltaPX / DPI

                    val newPosition = prevPosition + (scrollRate * deltaInches)
                    val newPercent = newPosition / positionMax
                    setPercent(newPercent)
                    onChanged?.invoke(newPercent)
                    Log.e("move", "$deltaPX $deltaInches $prevPosition $newPosition")
                }
            }

            val inside = (x >= 0) && (y >= 0) && (x < w) && (y < h)
            Log.e("touch", "$action $x $y $inside ${dif > MOVE_THRESHOLD}")
            return inside; // return true if inside so that we receive mouse move events
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun isVert() : Boolean { return direction == 1 }

    fun getPercent() : Float { return mPosition / positionMax }

    fun setPercent(percent: Float) {
        mPosition = max(0.0f, min(1.0f, percent)) * positionMax
        invalidate()
        requestLayout()
    }

//    fun setScrollRate(rate : Float) {
//        scrollRate = rate
//    }
//    fun setMax(pos_max: Float) {
//        val new_max = max(0.0f, pos_max)
//
//        val percent = mPosition / new_max // recalculate current percent based on new max
//        positionMax = pos_max
//        setPercent(percent) // in case max was set to less than position
//    }

}