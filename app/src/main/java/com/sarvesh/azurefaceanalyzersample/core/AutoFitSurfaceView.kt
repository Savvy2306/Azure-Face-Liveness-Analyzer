package com.sarvesh.azurefaceanalyzersample.core

import android.content.Context
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import android.view.ViewOutlineProvider
import kotlin.math.min
import kotlin.math.roundToInt

class AutoFitSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : SurfaceView(context, attrs, defStyle) {

    private var aspectRatio = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height)
        } else {
            val newWidth: Int
            val newHeight: Int
            val actualRatio = 1f
            if (width < height * actualRatio) {
                newHeight = height
                newWidth = (height * actualRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / actualRatio).roundToInt()
            }
            setMeasuredDimension(newWidth, newHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val halfWidth = w / 2f
        val halfHeight = h / 2f
        path.reset()
        path.addCircle(halfWidth, halfHeight, min(halfWidth, halfHeight), Path.Direction.CW)
        path.close()
    }

    private val path: Path = Path()

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                if (view != null && outline != null) {
                    val diameter = min(view.measuredHeight, view.measuredWidth)
                    val rect = Rect(
                        (view.measuredWidth - diameter) / 2,
                        (view.measuredHeight - diameter) / 2,
                        (view.measuredWidth + diameter) / 2,
                        (view.measuredHeight + diameter) / 2
                    )
                    outline.setRoundRect(rect, diameter / 2.0f)
                }
            }
        }
        clipToOutline = true
    }
}