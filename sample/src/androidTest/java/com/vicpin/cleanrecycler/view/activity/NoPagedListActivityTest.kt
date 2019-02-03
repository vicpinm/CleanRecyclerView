package com.vicpin.cleanrecycler.view.activity

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.whenever
import com.vicpin.cleanrecycler.sample.R
import com.vicpin.cleanrecycler.sample.data.ItemService
import com.vicpin.cleanrecycler.sample.di.AppComponent
import com.vicpin.cleanrecycler.sample.di.AppModule
import com.vicpin.cleanrecycler.sample.extensions.finishIdlingResource
import com.vicpin.cleanrecycler.sample.extensions.startIdlingResource
import com.vicpin.cleanrecycler.sample.view.activity.NoPagedListActivity
import com.vicpin.cleanrecycler.util.EspressoUtils.doActionOnRecyclerViewItem
import com.vicpin.cleanrecycler.util.EspressoUtils.doAssertionOverRecyclerView
import com.vicpin.cleanrecycler.util.getApplication
import com.vicpin.cleanrecycler.view.CleanRecyclerView
import com.vicpin.cleanrecycler.view.CleanRecyclerView.Event.DATA_LOADED
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy

/**
 * Created by Victor on 19/05/2017.
 */
@RunWith(AndroidJUnit4::class)
class NoPagedListActivityTest {

    @get:Rule
    val rule = DaggerMock.rule<AppComponent>(AppModule(getApplication())) {
        set { component -> getApplication().setAppComponent(component) }
    }

    @Spy
    lateinit var service: ItemService


    @Test
    fun testDataIsVisibleAfterLoad() {
        launchActivityWithIdlinResourceUntilEvent(DATA_LOADED)
        onView(withId(R.id.recyclerListView)).check(matches(isDisplayed()))
    }

    @Test
    fun testFirstPageIsLoaded() {
        launchActivityWithIdlinResourceUntilEvent(DATA_LOADED)
        doAssertionOverRecyclerView(assertion = matches(isDisplayed()), viewId =  R.id.row)
    }


    @Test
    fun testErrorTextIsVisibleWhenServiceReturnsError() {
        whenever(service.getData()).thenReturn(Single.error(Throwable()))
        launchActivity<NoPagedListActivity>()
        onView(withId(R.id.errorText)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyTextIsVisibleWhenServiceReturnsNoData() {
        whenever(service.getData()).thenReturn(Single.just(listOf()))
        launchActivity<NoPagedListActivity>()
        onView(withId(R.id.emptyText)).check(matches(isDisplayed()))
    }


    @Test
    fun testRowClickNavigatesToDetail() {
        launchActivityWithIdlinResourceUntilEvent(DATA_LOADED)
        doActionOnRecyclerViewItem(action = click(),viewId =  R.id.row)
        onView(withId(R.id.itemtitle)).check(matches(isDisplayed()))
    }

    private fun launchActivityWithIdlinResourceUntilEvent(event: CleanRecyclerView.Event) {
        startIdlingResource()

        launchActivity<NoPagedListActivity>().onActivity {
            it.getList().setEventListener { ev ->
                if(ev == event) {
                    finishIdlingResource()
                }
            }
        }
    }


}