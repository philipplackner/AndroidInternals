package com.plcoding.androidinternals

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class AnimatedCircleView(
    private val context: Context,
    private val attrs: AttributeSet? = null
): View(
    context, attrs
) {
    companion object {
        private const val TAG = "AnimatedCircleView"
    }
    
    private var width = 0
    private var height = 0

    private val ovalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        setShadowLayer(
            100f,
            0f,
            0f,
            Color.argb(0.4f, 0f, 0f, 0f)
        )
    }

    private val startColor = Color.RED
    private val endColor = Color.GREEN

    private val colorAnimator = ValueAnimator().apply {
        setFloatValues(0f, 1f)
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
    }

    private fun interpolateColor(fraction: Float): Int {
        return ArgbEvaluator().evaluate(fraction, startColor, endColor) as Int
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: View attached to window.")

        colorAnimator.start()
        colorAnimator.addUpdateListener {
            ovalPaint.color = interpolateColor(
                colorAnimator.animatedValue as Float
            )
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
        Log.d(TAG, "onSizeChanged: Size($w, $h)")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "onMeasure: Constraints($widthMeasureSpec, $heightMeasureSpec)")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(TAG, "onLayout: Position($left, $top, $right, $bottom)")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOval(
            width * 0.25f,
            height * 0.25f,
            width * 0.75f,
            height * 0.75f,
            ovalPaint
        )
        Log.d(TAG, "onDraw called.")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow: View detached from window.")

        colorAnimator.pause()
        colorAnimator.removeAllUpdateListeners()
    }
}