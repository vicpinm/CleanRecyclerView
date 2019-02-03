package com.vicpin.cleanrecycler.util

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.vicpin.cleanrecycler.sample.Application
import com.vicpin.kpresenteradapter.PresenterAdapter
import org.hamcrest.Description

fun getApplication() = ApplicationProvider.getApplicationContext() as Application
fun hasAdapterSize(size: Int) = AdapterCountMarcher(size)
fun ViewInteraction.scrollTo(position: Int) = perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
fun onViewId(@IdRes id: Int) = Espresso.onView(ViewMatchers.withId(id))

class AdapterCountMarcher(val size: Int): BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

    var currentItemsSize = 0

    override fun describeTo(description: Description) {
        description.appendText("adapter expected items count: $size, found $currentItemsSize")
    }

    override fun matchesSafely(recycler: RecyclerView): Boolean {
        this.currentItemsSize = recycler.adapter?.itemCount ?: -1
        return (recycler.adapter as? PresenterAdapter<*>)?.getData()?.size == size
    }
}