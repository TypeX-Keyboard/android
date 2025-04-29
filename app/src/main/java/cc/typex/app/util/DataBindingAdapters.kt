package cc.typex.app.util

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop

@BindingAdapter(
    value = ["imageUrl", "roundRadius", "borderWidth", "borderColor", "borderResizeImage", "centerCrop"],
    requireAll = false
)
fun setImageViewData(
    imageView: ImageView, imageUrl: String?,
    roundRadius: Float? = null,
    borderWidth: Float? = null,
    borderColor: Int? = null,
    borderResizeImage: Boolean? = null,
    centerCrop: Boolean? = false,
) {
    imageView.setImageDrawable(null)
    if (imageUrl.isNullOrEmpty()) return
    val requestManager = GlideApp.with(imageView)
    var request = requestManager.load(imageUrl)

    val transformations = mutableListOf<Transformation<Bitmap>>()
    if (centerCrop == true) {
        transformations.add(CenterCrop())
    }
    if ((roundRadius ?: 0f) > 0f) {
        transformations.add(
            RoundCornerBorder(
                (roundRadius ?: 0f).toInt(),
                (borderWidth ?: 0f).toInt(),
                borderColor ?: 0,
                borderResizeImage ?: false
            )
        )
    }
    if (transformations.isNotEmpty()) {
        request = request.transform(MultiTransformation(transformations))
    }
    request.into(imageView)
}


@BindingAdapter("visibleOrGone")
fun setViewVisibleOrGone(view: View, visible: Boolean?) {
    view.visibility = if (visible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("textColorRes")
fun setTextColorRes(view: TextView, colorRes: Int?) {
    view.setTextColor(
        ContextCompat.getColor(
            view.context,
            colorRes.takeIf { it != null && it > 0 } ?: return))
}