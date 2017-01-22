package com.vicpin.cleanrecyclerview.sample.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by victor on 21/1/17.
 */
fun ImageView.loadWithPicasso(imageUrl: String, cornerType: CornersTransformation.CornerType? = null) {
    visibility = View.VISIBLE
    if (!TextUtils.isEmpty(imageUrl)) {
        if(cornerType != null) {
            Glide.with(context).load(imageUrl).bitmapTransform(CornersTransformation(context, 6.toPx(context), 0, cornerType)).into(this)
        }
        else{
            Glide.with(context).load(imageUrl).into(this)
        }
    }
}

fun Int.toPx(context : Context) : Int {
    val displayMetrics = context.resources.displayMetrics
    return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Activity.startActivityWithTransition(intent : Intent, sharedView: View, transitionResId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val pair1 = Pair(sharedView, getString(transitionResId))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1)
        startActivity(intent, options.toBundle())
    } else {
        startActivity(intent)
    }
}