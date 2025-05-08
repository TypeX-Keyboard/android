package cc.typex.app.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import cc.typex.app.R

class DialoguePopupWindow(context: Context) : PopupWindow() {

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.dialogue_popup_view, null)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = false
        isOutsideTouchable = true

    }

    fun showAbove(anchorView: View) {
        contentView.measure(0, 0)
        val xOffset = 0
        val yOffset = -contentView.measuredHeight
        showAsDropDown(anchorView, xOffset, yOffset)
    }
}