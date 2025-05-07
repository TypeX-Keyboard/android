package cc.typex.app.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import cc.typex.app.R

class AppToolbarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val ivIconLeft: ImageView
    private val tvTitle: TextView

    init {
        View.inflate(context, R.layout.toolbar_view, this)
        ivIconLeft = findViewById(R.id.iv_icon_left)
        tvTitle = findViewById(R.id.tv_title)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AppToolbarView)
        ivIconLeft.setImageDrawable(
            ta.getDrawable(R.styleable.AppToolbarView_etv_iconLeft)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_toolbar_back)
        )
        tvTitle.text = ta.getString(R.styleable.AppToolbarView_etv_title)
        ta.recycle()
    }

    fun setIconLeftClickListener(listener: OnClickListener) {
        ivIconLeft.setOnClickListener(listener)
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }
}