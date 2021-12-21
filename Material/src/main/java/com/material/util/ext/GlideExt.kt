package com.material.util.ext

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.material.R


fun ImageView.load(imageUrl: String?, @DrawableRes placeHolder: Int? = null, endAction: (() -> Unit)? = null) {
    Glide.with(context.applicationContext)
        .load(imageUrl)
        .placeholder(placeHolder ?: R.drawable.ic_baseline_image_100)
        .apply(RequestOptions().override(this.width, this.height).fitCenter())
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                endAction?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                endAction?.invoke()
                return false
            }
        })
        .into(this)
}