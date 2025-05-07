package cc.typex.app.widget.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class CustomProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val backgroundRect = RectF()
    private val progressRect = RectF()
    private val clipPath = Path()

    private var progress = 0
    private var maxProgress = 100

    init {
        // 背景画笔设置
        backgroundPaint.color = Color.parseColor("#80FFFFFF") // 50% 透明白色
        backgroundPaint.style = Paint.Style.FILL

        // 进度画笔设置
        progressPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 设置圆角半径为高度的一半，形成半圆形端点
        val cornerRadius = h / 2f

        // 设置背景矩形
        backgroundRect.set(0f, 0f, w.toFloat(), h.toFloat())

        // 设置渐变色
        progressPaint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            Color.parseColor("#5E24FF"),
            Color.parseColor("#682FCB"),
            Shader.TileMode.CLAMP
        )

        clipPath.reset()
        val radii = floatArrayOf(
            cornerRadius, cornerRadius,
            cornerRadius, cornerRadius,
            cornerRadius, cornerRadius,
            cornerRadius, cornerRadius,
        )
        clipPath.addRoundRect(backgroundRect, radii, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.clipPath(clipPath)
        canvas.drawRect(backgroundRect, backgroundPaint)

        val progressWidth =
            (width * (progress.toFloat() / maxProgress)).coerceAtMost(width.toFloat())
        progressRect.set(0f, 0f, progressWidth, height.toFloat())
        canvas.drawRect(progressRect, progressPaint)
    }

    /**
     * 设置当前进度
     */
    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, maxProgress)
        invalidate() // 重绘视图
    }

    /**
     * 设置最大进度
     */
    fun setMaxProgress(maxProgress: Int) {
        this.maxProgress = maxProgress
        invalidate()
    }
}