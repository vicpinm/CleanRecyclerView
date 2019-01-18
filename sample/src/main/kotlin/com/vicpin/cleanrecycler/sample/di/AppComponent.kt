package com.vicpin.cleanrecycler.sample.di

import android.app.Application
import com.vicpin.cleanrecycler.sample.view.activity.AnnotatedListActivity
import com.vicpin.cleanrecycler.sample.view.activity.MappedListActivity
import com.vicpin.cleanrecycler.sample.view.activity.NoPagedListActivity
import com.vicpin.cleanrecycler.sample.view.activity.PagedListActivity

import dagger.Component
import javax.inject.Singleton

/**
 * Created by Victor on 29/05/2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun application(): Application
    fun inject(mainActivity: NoPagedListActivity)
    fun inject(mainActivity: PagedListActivity)
    fun inject(mappedListActivity: MappedListActivity)
    fun inject(annotatedListActivity: AnnotatedListActivity)

}
