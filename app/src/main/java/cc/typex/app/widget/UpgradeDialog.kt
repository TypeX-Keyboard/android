package cc.typex.app.widget

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import cc.typex.app.R

class UpgradeDialog(context: Context) : Dialog(context) {

    private val tvVersionName: TextView
    private val tvUpdates: TextView
    private val tvConfirm: TextView
    private val tvSkip: TextView
    private val groupUpdates: Group

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.upgrade_dialog)
        tvVersionName = findViewById(R.id.tv_version_name)
        tvUpdates = findViewById(R.id.tv_updates)
        tvConfirm = findViewById(R.id.tv_confirm)
        tvSkip = findViewById(R.id.tv_skip)
        groupUpdates = findViewById(R.id.group_info)

        groupUpdates.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.CENTER
            attributes = params
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun setVersionName(versionName: String): UpgradeDialog {
        tvVersionName.text = versionName
        return this
    }

    fun setUpdateInfo(updateInfo: String): UpgradeDialog {
        if (updateInfo.isNotBlank()) {
            groupUpdates.visibility = View.VISIBLE
            tvUpdates.text = updateInfo
        } else {
            groupUpdates.visibility = View.GONE
        }
        return this
    }

    fun setOnClickConfirmListener(listener: View.OnClickListener): UpgradeDialog {
        tvConfirm.setOnClickListener(listener)
        return this
    }

    fun setOnClickSkipListener(listener: View.OnClickListener): UpgradeDialog {
        tvSkip.setOnClickListener(listener)
        return this
    }

    companion object {
        fun gotoMarket(context: Context) {
            val uri = Uri.parse("market://details?id=${context.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    )
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(webIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}