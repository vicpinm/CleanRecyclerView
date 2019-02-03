package com.vicpin.cleanrecycler.view.activity

import android.os.Handler
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.after
import com.nhaarman.mockito_kotlin.whenever
import com.vicpin.cleanrecycler.sample.R
import com.vicpin.cleanrecycler.sample.data.ItemCache
import com.vicpin.cleanrecycler.sample.data.ItemService
import com.vicpin.cleanrecycler.sample.di.AppComponent
import com.vicpin.cleanrecycler.sample.di.AppModule
import com.vicpin.cleanrecycler.sample.extensions.finishIdlingResource
import com.vicpin.cleanrecycler.sample.extensions.startIdlingResource
import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.cleanrecycler.sample.view.activity.NoPagedListActivity
import com.vicpin.cleanrecycler.util.EspressoUtils.doActionOnRecyclerViewItem
import com.vicpin.cleanrecycler.util.getApplication
import com.vicpin.cleanrecycler.util.hasAdapterSize
import com.vicpin.cleanrecycler.util.onViewId
import com.vicpin.cleanrecycler.util.scrollTo
import com.vicpin.cleanrecycler.view.CleanRecyclerView
import io.reactivex.Flowable
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMock
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Victor on 19/05/2017.
 */
@RunWith(AndroidJUnit4::class)
class NoPagedListActivityTest {

    val NUM_ITEMS = 50

    @get:Rule
    val rule = DaggerMock.rule<AppComponent>(AppModule(getApplication())) {
        set { component -> getApplication().setAppComponent(component) }
    }

    @Spy
    lateinit var service: ItemService

    @Spy
    lateinit var cache: ItemCache

    @Test
    fun testProgressIsVisibleWhileDataIsLoading() {
        //Given: repository with NUM_ITEMS (we set a delay so that progress is visible for a second)
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)).delay(1, TimeUnit.SECONDS))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: progress is displayed
        onViewId(R.id.progress).check(matches(isDisplayed()))
    }

    @Test
    fun testProgressIsNoLongerVisibleAfterDataLoad() {
        //Given: repository with NUM_ITEMS
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: progress is not displayed
        onViewId(R.id.progress).check(matches(not(isDisplayed())))
    }

    @Test
    fun testDataIsVisibleAfterLoad() {
        //Given: repository with NUM_ITEMS
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: recyclerView is displayed after data is loaded
        onViewId(R.id.recyclerListView).check(matches(isDisplayed()))
    }

    @Test
    fun testDataLoaded() {
        //Given: repository with NUM_ITEMS
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: collection is loaded with size NUM_ITEMS
        onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(NUM_ITEMS)))
    }

    @Test
    fun testAllRowsAreDisplayed() {
        //Given: repository with NUM_ITEMS
        val collection = listOfItems(NUM_ITEMS)
        whenever(service.getData()).thenReturn(Single.just(collection))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: each row shows correct text
        collection.forEachIndexed { index, item ->
            onViewId(R.id.recyclerListView).scrollTo(index)
            onView(ViewMatchers.withText(item.title)).check(matches(isDisplayed()))
        }
    }


    @Test
    fun testErrorTextIsVisibleWhenServiceReturnsError() {
        //Given: repository which returns exception
        whenever(service.getData()).thenReturn(Single.error(Throwable()))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: error placeholder is shown
        onViewId(R.id.errorText).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyTextIsVisibleWhenServiceReturnsNoData() {
        //Given: repository which return empty collection
        whenever(service.getData()).thenReturn(Single.just(listOf()))

        //When: activity is launched
        launchActivity<NoPagedListActivity>()

        //Then: empty placeholder is shown
        onViewId(R.id.emptyText).check(matches(isDisplayed()))
    }


    @Test
    fun testRowClickNavigatesToDetail() {
        //Given: repository with NUM_ITEMS
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)))

        //When: activity is launched and click is perform on first row
        launchActivity<NoPagedListActivity>()
        doActionOnRecyclerViewItem(position = 0, action = click(), viewId = R.id.row)

        //Then: details activity is opened
        onViewId(R.id.itemtitle).check(matches(isDisplayed()))
    }

    @Test
    fun testRefreshIsNotShowedWhileDataIsLoadingForTheFirstTime() {
        //Given: repository with NUM_ITEMS
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)).delay(1, TimeUnit.SECONDS))

        //When: activity is launched
        launchActivity<NoPagedListActivity>().onActivity {

            //Then: refresh is not active
            assertThat(it.getList().refresh?.isRefreshing).isFalse()
        }

    }

    @Test
    fun test_first_serviceReturnsException_then_serviceReturnsData() {

        //Part ONE: service datasource returns exception
        //************************************************

        //Given: repository which returns exception
        whenever(service.getData()).thenReturn(Single.error(Throwable()))

        lateinit var act: NoPagedListActivity

        //When: activity is launched
        launchActivity<NoPagedListActivity>().onActivity { act = it }

        //Then: error placeholder is shown
        onViewId(R.id.errorText).check(matches(isDisplayed()))


        //Part TWO: service datasource returns data after retry
        //****************************************************

        //Given: repository that returns data after one second
        startIdlingResource()
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)).delay(1, TimeUnit.SECONDS))


        //When: retry to load data with reloadData method
        act.getList().apply {

            act.runOnUiThread {
                reloadData()

                //Then: assert that swipeToRefresh widgets is showing
                assertThat(refresh?.isRefreshing).isTrue()
            }

            setEventListener {
                if(it == CleanRecyclerView.Event.DATA_LOADED) {
                    finishIdlingResource()
                }
            }

            //Then: collection is loaded with size NUM_ITEMS
            onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(NUM_ITEMS)))
        }

    }

    @Test
    fun test_first_serviceReturnsData_then_serviceReturnsEmptyResponse() {

        //Part ONE: service datasource data
        //************************************************

        //Given: repository which returns data
        whenever(service.getData()).thenReturn(Single.just(listOfItems(NUM_ITEMS)))

        lateinit var act: NoPagedListActivity

        //When: activity is launched
        launchActivity<NoPagedListActivity>().onActivity { act = it }

        //Then: collection is loaded with size NUM_ITEMS
        onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(NUM_ITEMS)))


        //Part TWO: service datasource returns empty response
        //****************************************************

        //Given: repository that returns empty response after one second
        startIdlingResource()
        whenever(service.getData()).thenReturn(Single.just(listOf<Item>()).delay(1, TimeUnit.SECONDS))


        //When: retry to load data with reloadData method
        act.getList().apply {

            act.runOnUiThread {
                reloadData()

                //Then: assert that swipeToRefresh widgets is showing
                assertThat(refresh?.isRefreshing).isTrue()
            }

            setEventListener {
                if(it == CleanRecyclerView.Event.EMPTY_LAYOUT_SHOWED) {
                    finishIdlingResource()
                }
            }

            //Then: collection is loaded with size NUM_ITEMS
            onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(0)))

            //Empty placeholder is shown
            onViewId(R.id.emptyText).check(matches(isDisplayed()))
        }

    }

    @Test
    fun test_first_cacheReturnsItems_then_serviceReturnsMoreItems() {

        val CACHE_ITEMS_SIZE = 1
        val SERVICE_ITEMS_SIZE = 2

        //Given: cache return 1 item
        cache.memoryCache = listOfItems(CACHE_ITEMS_SIZE).toMutableList()

        //Service returns more items
        whenever(service.getData()).thenReturn(Single.just(listOfItems(SERVICE_ITEMS_SIZE)).delay(2, TimeUnit.SECONDS))

        lateinit var act: NoPagedListActivity

        //When: activity is launched
        launchActivity<NoPagedListActivity>().onActivity { act = it }

        //Then: collection is loaded first with cache items size and refresing is showing
        onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(CACHE_ITEMS_SIZE)))
        assertThat(act.getList().refresh?.isRefreshing).isTrue()

        //Set idling resource in order to wait server response
        startIdlingResource()
        act.getList().setEventListener {
            if(it == CleanRecyclerView.Event.DATA_LOADED) {
                finishIdlingResource()
            }
        }

        //After server response, collection is updated
        onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(SERVICE_ITEMS_SIZE)))

    }

    @Test
    fun test_first_cacheReturnsItems_then_serviceReturnsException() {

        val CACHE_ITEMS_SIZE = 1

        //Given: cache return 1 item
        cache.memoryCache = listOfItems(CACHE_ITEMS_SIZE).toMutableList()

        //Service returns exception after some delay
        whenever(service.getData()).thenReturn(errorAfterSeconds(1))

        lateinit var act: NoPagedListActivity

        //Set idling resource in order to wait server response
        startIdlingResource()

        //When: activity is launched
        launchActivity<NoPagedListActivity>().onActivity { act = it

            act.getList().setEventListener {
                if (it == CleanRecyclerView.Event.CONNECTION_ERROR) {
                    assertThat(act.getList().refresh?.isRefreshing).isFalse()
                    finishIdlingResource()
                }
            }
        }

        //Then: collection is loaded with cache items
        onViewId(R.id.recyclerListView).check(matches(hasAdapterSize(CACHE_ITEMS_SIZE)))

    }


    private fun listOfItems(size: Int) = List(size) {
        Item("Material card $it", "Description number $it",
                "https://lh3.googleusercontent.com/-JjdCDI4-CJg/V0v8Tk3ng7I/AAAAAAABq7o/6fRDNinKmzoURluAg29-hp8LPysvUc_PA/w800-h800/Material-Design-3.jpg")
    }

    private fun errorAfterSeconds(seconds: Long) = Single.just<List<Item>>(listOf()).delay(seconds, TimeUnit.SECONDS).flatMap { Single.error<List<Item>>(IOException("Connection error")) }

}