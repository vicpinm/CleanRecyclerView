package com.vicpin.cleanrecycler.view.activity

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.whenever
import com.vicpin.cleanrecycler.sample.R
import com.vicpin.cleanrecycler.sample.data.ItemService
import com.vicpin.cleanrecycler.sample.di.AppComponent
import com.vicpin.cleanrecycler.sample.di.AppModule
import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.cleanrecycler.sample.view.activity.NoPagedListActivity
import com.vicpin.cleanrecycler.util.EspressoUtils.doActionOnRecyclerViewItem
import com.vicpin.cleanrecycler.util.getApplication
import com.vicpin.cleanrecycler.util.hasAdapterSize
import com.vicpin.cleanrecycler.util.onViewId
import com.vicpin.cleanrecycler.util.scrollTo
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

    val NUM_ITEMS = 50

    @get:Rule
    val rule = DaggerMock.rule<AppComponent>(AppModule(getApplication())) {
        set { component -> getApplication().setAppComponent(component) }
    }

    @Spy
    lateinit var service: ItemService


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

    private fun listOfItems(size: Int) = List(size) {
        Item("Material card $it", "Description number $it",
                "https://lh3.googleusercontent.com/-JjdCDI4-CJg/V0v8Tk3ng7I/AAAAAAABq7o/6fRDNinKmzoURluAg29-hp8LPysvUc_PA/w800-h800/Material-Design-3.jpg")
    }

}