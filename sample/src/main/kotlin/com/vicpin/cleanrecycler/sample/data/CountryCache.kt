package com.vicpin.cleanrecycler.sample.data

import com.vicpin.cleanrecycler.annotation.DataSource
import com.vicpin.cleanrecycler.annotation.Mapper
import com.vicpin.cleanrecycler.repository.datasource.CacheDataSource
import com.vicpin.cleanrecycler.sample.model.Country
import com.vicpin.cleanrecycler.sample.model.Item
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

@DataSource
open class CountryCache : CacheDataSource<Country> {

    var memoryCache = mutableListOf<Country>()
    var memoryCacheChangeListener = PublishSubject.create<Boolean>()

    override fun getData() = Flowable.create<List<Country>>({ emmiter ->
        memoryCacheChangeListener.subscribe {
            emmiter.onNext(memoryCache)
        }

        emmiter.onNext(memoryCache)


    }, BackpressureStrategy.DROP)

    private fun clearData() {
        memoryCache.clear()
    }


    override fun saveData(clearOldData: Boolean, data: List<Country>) {
        if (clearOldData) {
            clearData()
        }

        memoryCache.addAll(data)

        //Notify change
        memoryCacheChangeListener.onNext(true)
    }
}