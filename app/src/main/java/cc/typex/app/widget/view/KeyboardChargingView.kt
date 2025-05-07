package cc.typex.app.widget.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import cc.typex.app.R

class KeyboardChargingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val pbProgress: CustomProgressBar
    private val tvProgress: TextView
    private val tvCharging: TextView
    private val tvNotCharging: TextView

    private var enable: Boolean = false
    private var progress: Int = 0
    private var max: Int = 100

    private val clCharging: View
    private val vObtained: View

    init {
        View.inflate(context, R.layout.keyboard_charging_view, this)
        pbProgress = findViewById(R.id.pb_progress)
        tvProgress = findViewById(R.id.tv_progress)
        tvCharging = findViewById(R.id.tv_charging)
        tvNotCharging = findViewById(R.id.tv_not_charging)

        clCharging = findViewById(R.id.cl_charging)
        vObtained = findViewById(R.id.v_obtained)

        updateViews()
    }

    fun setEnable(enable: Boolean) {
        this.enable = enable
        updateViews()
    }

    fun setMax(max: Int) {
        this.max = max
        updateViews()
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        updateViews()
    }

    private fun updateViews() {
        if (enable) {
            pbProgress.visibility = View.VISIBLE
            tvCharging.visibility = View.VISIBLE
            tvNotCharging.visibility = View.GONE

            @SuppressLint("SetTextI18n")
            tvProgress.text = "$progress/$max"
            pbProgress.setMaxProgress(max)
            pbProgress.setProgress(progress)
        } else {
            pbProgress.visibility = View.GONE
            tvCharging.visibility = View.GONE
            tvNotCharging.visibility = View.VISIBLE
        }
    }

    fun showAnimation() {
        if (!enable) return
        val stage1 = ValueAnimator.ofFloat(0f, height.toFloat()).apply {
            duration = 400
            addUpdateListener {
                clCharging.translationY = -(it.animatedValue as Float)
                vObtained.translationY = -(it.animatedValue as Float)
            }
        }
        val stage2 = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
        }
        val stage3 = ValueAnimator.ofFloat(0f, height.toFloat()).apply {
            duration = 400
            addUpdateListener {
                clCharging.translationY = height - (it.animatedValue as Float)
                vObtained.translationY = -(height + (it.animatedValue as Float))
            }
        }
        val animatorSet = AnimatorSet().apply {
            playSequentially(stage1, stage2, stage3)
        }
        animatorSet.start()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        for (child in children) {
            child.setOnClickListener(l)
        }
    }
}