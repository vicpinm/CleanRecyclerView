package com.vicpin.cleanrecycler.sample.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.vicpin.cleanrecycler.sample.Application
import com.vicpin.cleanrecycler.sample.di.AppComponent
import java.util.concurrent.atomic.AtomicBoolean
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource


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

fun startIdlingResource() = IdlingResourceManager.start()
fun finishIdlingResource() = IdlingResourceManager.finish()

object IdlingResourceManager {

    private var instance: SimpleIdlingResource? = null
    var autoRegister = true

    fun register() {
        if(instance == null) {
            start()
        }
        IdlingRegistry.getInstance().register(instance)
    }

    fun start() {
        if(instance == null) {
            instance = SimpleIdlingResource()
            if(autoRegister) {
                IdlingRegistry.getInstance().register(instance)
            }
        }
    }

    fun finish() {
        instance?.apply {
            Handler().postDelayed({
                finish()
                IdlingRegistry.getInstance().unregister(this)
                instance = null
            },100)

        }
    }
}

class SimpleIdlingResource: IdlingResource {

    private var callback: IdlingResource.ResourceCallback? = null
    private val isIdleNow = AtomicBoolean(false)

    override fun getName() = this.javaClass.name
    override fun isIdleNow() = isIdleNow.get()

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    fun finish() {
        this.isIdleNow.set(true)
        callback?.onTransitionToIdle()
    }
}



