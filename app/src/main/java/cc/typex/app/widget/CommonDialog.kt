package cc.typex.app.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import cc.typex.app.R
import cc.typex.app.databinding.CommonDialogBinding

class CommonDialog(context: Context, private val builder: Builder) : Dialog(context) {

    private lateinit var binding: CommonDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.common_dialog,
            null,
            false
        )
        setContentView(binding.root)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.CENTER
            attributes = params
        }

        setCancelable(builder.cancelable)
        setCanceledOnTouchOutside(builder.cancelableTouchOutside)
        if (builder.titleTextRes != 0) {
            binding.tvTitle.text = context.getString(builder.titleTextRes)
        } else {
            binding.tvTitle.visibility = View.GONE
        }
        if (builder.textTextRes != 0) {
            binding.tvText.text = context.getString(builder.textTextRes)
        } else {
            binding.tvText.visibility = View.GONE
        }
        if (builder.iconDrawableRes != 0) {
            binding.ivIcon.setImageResource(builder.iconDrawableRes)
        } else {
            binding.ivIcon.visibility = View.GONE
        }
        if (builder.positiveTextRes != 0) {
            binding.tvPositive.text = context.getString(builder.positiveTextRes)
            binding.tvPositive.setOnClickListener {
                builder.positiveListener()
                dismiss()
            }
        } else {
            binding.tvPositive.visibility = View.GONE
        }
        if (builder.negativeTextRes != 0) {
            binding.tvNegative.text = context.getString(builder.negativeTextRes)
            binding.tvNegative.setOnClickListener {
                builder.negativeListener()
                dismiss()
            }
        } else {
            binding.tvNegative.visibility = View.GONE
        }
    }

    class Builder(private val context: Context) {

        var titleTextRes = 0
        var textTextRes = 0
        var iconDrawableRes = 0
        var positiveTextRes = 0
        var positiveListener: () -> Unit = {}
        var negativeTextRes = 0
        var negativeListener: () -> Unit = {}
        var cancelable = false
        var cancelableTouchOutside = false

        fun setTitleTextRes(titleTextRes: Int): Builder {
            this.titleTextRes = titleTextRes
            return this
        }

        fun setTextTextRes(textTextRes: Int): Builder {
            this.textTextRes = textTextRes
            return this
        }

        fun setIconDrawableRes(iconDrawableRes: Int): Builder {
            this.iconDrawableRes = iconDrawableRes
            return this
        }

        fun setPositiveButton(positiveTextRes: Int, positiveListener: () -> Unit): Builder {
            this.positiveTextRes = positiveTextRes
            this.positiveListener = positiveListener
            return this
        }

        fun setNegativeButton(negativeTextRes: Int, negativeListener: () -> Unit): Builder {
            this.negativeTextRes = negativeTextRes
            this.negativeListener = negativeListener
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setCancelableTouchOutside(cancelable: Boolean): Builder {
            this.cancelableTouchOutside = cancelable
            return this
        }

        fun build(): CommonDialog {
            val dialog = CommonDialog(context, this)
            return dialog
        }
    }
}