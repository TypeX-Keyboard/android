package cc.typex.app.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import cc.typex.app.R

class ErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val tvButton: TextView

    init {
        View.inflate(context, R.layout.error_view, this)
        tvButton = findViewById(R.id.tv_button)
    }

    fun setButtonClickListener(listener: OnClickListener?) {
        tvButton.setOnClickListener(listener)
    }
}