package cc.typex.app.widget.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import cc.typex.app.R

class KeyboardCandidateButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val vWallet: View
    private val vRecent: View

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation = HORIZONTAL
        View.inflate(context, R.layout.keyboard_candidate_button, this)
        vWallet = findViewById(R.id.v_wallet)
        vRecent = findViewById(R.id.v_recent)
    }

    fun setWalletClickListener(listener: OnClickListener) {
        vWallet.setOnClickListener(listener)
    }

    fun setRecentClickListener(listener: OnClickListener) {
        vRecent.setOnClickListener(listener)
    }
}