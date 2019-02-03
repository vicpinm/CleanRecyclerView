package com.vicpin.cleanrecycler.util

import android.view.View

import org.hamcrest.Matcher
import org.hamcrest.Matchers

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import com.vicpin.cleanrecycler.sample.R

import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`

/**
 * Created by Victor on 09/02/16.
 */
object EspressoUtils {

    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v?.performClick()
            }
        }
    }

    private class ActionOnItemViewAtPositionViewAction
    constructor(private val position: Int, @param:IdRes private val viewId: Int, private val viewAction: ViewAction) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf<View>(ViewMatchers.isAssignableFrom(RecyclerView::class.java), ViewMatchers.isDisplayed())
        }

        override fun getDescription(): String {
            return ("actionOnItemAtPosition performing ViewAction: "
                    + this.viewAction.description
                    + " on item at position: "
                    + this.position)
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            ScrollToPositionViewAction(this.position).perform(uiController, view)
            uiController.loopMainThreadUntilIdle()

            val targetView = recyclerView.getChildAt(if (this.position < recyclerView.childCount) this.position else recyclerView.childCount - 1).findViewById<View>(this.viewId)

            if (targetView == null) {
                throw PerformException.Builder().withActionDescription(this.toString())
                        .withViewDescription(

                                HumanReadables.describe(view))
                        .withCause(IllegalStateException(
                                "No view with id "
                                        + this.viewId
                                        + " found at position: "
                                        + this.position))
                        .build()
            } else {
                this.viewAction.perform(uiController, targetView)
            }
        }
    }

    private class ScrollToPositionViewAction constructor(private val position: Int) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf<View>(ViewMatchers.isAssignableFrom(RecyclerView::class.java), ViewMatchers.isDisplayed())
        }

        override fun getDescription(): String {
            return "scroll RecyclerView to position: " + this.position
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            recyclerView.scrollToPosition(this.position)
        }
    }

    private class AssertionOnItemViewAtPosition
    constructor(private val position: Int, @param:IdRes private val viewId: Int, private val viewAssertion: ViewAssertion) : ViewAssertion {

        val constraints: Matcher<View>
            get() = Matchers.allOf<View>(ViewMatchers.isAssignableFrom(RecyclerView::class.java), ViewMatchers.isDisplayed())

        override fun check(view: View, noViewFoundException: NoMatchingViewException) {
            val recyclerView = view as RecyclerView
            scrollToPosition(this.position)

            val targetView = recyclerView.getChildAt(if (this.position < recyclerView.childCount) this.position else recyclerView.childCount - 1).findViewById<View>(this.viewId)

            if (targetView == null) {
                throw PerformException.Builder().withActionDescription(this.toString())
                        .withViewDescription(

                                HumanReadables.describe(view))
                        .withCause(IllegalStateException(
                                "No view with id "
                                        + this.viewId
                                        + " found at position: "
                                        + this.position))
                        .build()
            } else {
                this.viewAssertion.check(targetView, noViewFoundException)
            }
        }
    }

    //***********************************************************************************************************************
    // Auxiliary methods
    //***********************************************************************************************************************

    fun withRecyclerView(recyclerViewId: Int) = RecyclerViewMatcher(recyclerViewId)

    fun scrollToPosition(position: Int): ViewAction = ScrollToPositionViewAction(position)

    fun actionOnItemViewAtPosition(position: Int, @IdRes viewId: Int, viewAction: ViewAction): ViewAction {
        return ActionOnItemViewAtPositionViewAction(position, viewId, viewAction)
    }

    fun assertionOnItemViewAtPosition(position: Int, @IdRes viewId: Int, assertion: ViewAssertion): ViewAssertion {
        return AssertionOnItemViewAtPosition(position, viewId, assertion)
    }

    //***********************************************************************************************************************
    // Utility methods to perform multiple actions or assertions over AdapterView and RecyclerView
    //***********************************************************************************************************************

    fun <T> doActionOverAdapterView(itemClass: Class<*>, count: Int, action: ViewAction, viewId: Int) {

        for (i in 0 until count) {
            onData(allOf<T>(`is`<T>(instanceOf<T>(itemClass)))).atPosition(i)
                    .onChildView(withId(viewId)).perform(action)
        }

    }

    fun <T> doAssertionOverAdapterView(itemClass: Class<*>, count: Int, assertion: ViewAssertion, viewId: Int) {

        for (i in 0 until count) {
            onData(allOf<T>(`is`<T>(instanceOf<T>(itemClass)))).atPosition(i)
                    .onChildView(withId(viewId)).check(assertion)
        }

    }

    fun doActionOverRecyclerView(@IdRes recyclerId: Int, count: Int, action: ViewAction, viewId: Int) {

        for (i in 0 until count) {
            onView(withId(recyclerId)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(i, clickChildViewWithId(viewId)))
        }
    }


    fun doActionOnRecyclerViewItem(@IdRes recyclerId: Int = R.id.recyclerListView, position: Int = 0, action: ViewAction, viewId: Int) {
        onView(withId(recyclerId)).perform(actionOnItemViewAtPosition(position, viewId, action))
    }

    fun scrollListToPosition(@IdRes recyclerId: Int, position: Int) {
        onView(withId(recyclerId)).perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }

}