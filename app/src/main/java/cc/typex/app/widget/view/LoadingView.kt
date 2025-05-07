package cc.typex.app.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import cc.typex.app.R
import com.airbnb.lottie.LottieAnimationView

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val loading: LottieAnimationView

    init {
        setBackgroundColor(Color.parseColor("#B3000000"))
        View.inflate(context, R.layout.loading_view, this)
        loading = findViewById(R.id.lav_loading)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loading.playAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loading.cancelAnimation()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}