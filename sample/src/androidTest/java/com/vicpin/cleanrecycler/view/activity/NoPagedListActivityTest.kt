package com.vicpin.cleanrecyclerview.view.activity

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import com.vicpin.cleanrecyclerview.sample.Application
import com.vicpin.cleanrecyclerview.sample.R
import com.vicpin.cleanrecyclerview.sample.data.ItemCache
import com.vicpin.cleanrecyclerview.sample.data.ItemPagedService
import com.vicpin.cleanrecyclerview.sample.data.ItemService
import com.vicpin.cleanrecyclerview.sample.di.AppComponent
import com.vicpin.cleanrecyclerview.sample.di.AppModule
import com.vicpin.cleanrecyclerview.sample.view.activity.MainActivity
import com.vicpin.cleanrecyclerview.util.TestUtils.*
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy



/**
 * Created by Victor on 19/05/2017.
 */
@RunWith(AndroidJUnit4::class)
class NoPagedListActivityTest {

    val ITEMS_PER_PAGE = 5

    @Spy lateinit var mPagedService : ItemPagedService
    @Spy lateinit var mNoPagedService : ItemService
    @Spy lateinit var mCache : ItemCache

    @get:Rule
    val mActivityRule = ActivityTestRule<MainActivity>(MainActivity::class.java, true, false)

    @get:Rule var daggerRule = DaggerMockRule(AppComponent::class.java, AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application))
            .set({ component ->
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
                app.setAppComponent(component)
            })


    @Before
    fun setUp(){
        reset(mPagedService)
        mCache.clearData()
     //   mPagedService.description = ""

        // val act = mActivityRule.activity
        //Injector.injectTo(mPagedService, mActivityRule.activity)
    }


    @Test
    fun testDataIsVisibleAfterLoad() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.recyclerListView)).check(matches(isDisplayed()))
    }

    @Test
    fun testFirstPageIsLoaded() {
        mActivityRule.launchActivity(Intent())
        doAssertionOverRecyclerView(ITEMS_PER_PAGE, matches(isDisplayed()), R.id.row)
    }

    @Test
    fun testNewPageIsLoadedAfterScrollToBottom(){
        mActivityRule.launchActivity(Intent())
        scrollListToPosition(ITEMS_PER_PAGE)
        doAssertionOverRecyclerView(ITEMS_PER_PAGE * 2, matches(isDisplayed()), R.id.row)
    }

    @Test
    fun testErrorTextIsVisibleWhenServiceReturnsError(){
        whenever(mPagedService.getData(any())).thenReturn(Single.error(Throwable()))
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.errorText)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyTextIsVisibleWhenServiceReturnsNoData(){
        whenever(mPagedService.getData(any())).thenReturn(Single.just(listOf()))
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.emptyText)).check(matches(isDisplayed()))
    }

    @Test
    fun testNoPagedDataIsVisibleAfterLoad(){
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.paginated)).perform(click())
        onView(withId(R.id.recyclerListView)).check(matches(isDisplayed()))
    }

    @Test
    fun testErrorTextIsVisibleWhenNoPagedAndServiceReturnsError(){
        whenever(mNoPagedService.getData()).thenReturn(Single.error(Throwable()))
        mActivityRule.launchActivity(Intent())
        mCache.clearData()
        onView(withId(R.id.paginated)).perform(click())
        onView(withId(R.id.errorText)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyTextIsVisibleWhenNoPagedAndServiceReturnsNoData(){
        whenever(mNoPagedService.getData()).thenReturn(Single.just(listOf()))
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.paginated)).perform(click())
        onView(withId(R.id.emptyText)).check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnCellNavigatesToDetail(){
        mActivityRule.launchActivity(Intent())
        doActionOnRecyclerViewItem(0, click(), R.id.row)
        onView(withId(R.id.itemtitle)).check(matches(isDisplayed()))
    }


}