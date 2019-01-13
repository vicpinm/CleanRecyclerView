package com.vicpin.cleanrecyclerview.sample.di

import android.app.Application
import com.vicpin.cleanrecyclerview.sample.data.ItemCache
import com.vicpin.cleanrecyclerview.sample.data.ItemPagedService
import com.vicpin.cleanrecyclerview.sample.data.ItemService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Victor on 29/05/2017.
 */
@Module
open class AppModule(val mApplication: Application) {

    @Provides @Singleton open fun provideApplication() = mApplication

    @Provides @Singleton open fun itemPagedService() = ItemPagedService()

    @Provides @Singleton open fun noPagedService() = ItemService()

    @Provides @Singleton open fun cache() = ItemCache()

}