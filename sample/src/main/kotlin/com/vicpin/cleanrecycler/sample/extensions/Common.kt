package com.vicpin.cleanrecyclerview.sample.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.vicpin.cleanrecyclerview.sample.Application
import com.vicpin.cleanrecyclerview.sample.di.AppComponent

/**
 * Created by victor on 21/1/17.
 */
fun ImageView.load(imageUrl: String) {
    visibility = View.VISIBLE
    if (!TextUtils.isEmpty(imageUrl)) {
        Picasso.with(context).load(imageUrl).into(this)
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


val AppCompatActivity.injector: AppComponent
    get() = Application.instance.getAppComponent()

