package com.vicpin.cleanrecycler.sample.data

import com.vicpin.cleanrecycler.repository.datasource.CacheDataSource
import com.vicpin.cleanrecycler.sample.model.Item

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

/**
 * Created by victor on 21/1/17.
 */
open class ItemCache: CacheDataSource<Item> {

    var memoryCache = mutableListOf<Item>()
    var memoryCacheChangeListener = PublishSubject.create<Boolean>()

    override fun getData() = Flowable.create<List<Item>>({emmiter ->
        memoryCacheChangeListener.subscribe {
            emmiter.onNext(memoryCache)
        }

        emmiter.onNext(memoryCache)


    }, BackpressureStrategy.DROP)

    fun clearData() {
        memoryCache.clear()
    }


    override fun saveData(clearOldData: Boolean, data: List<Item>) {
        if(clearOldData) {
            clearData()
        }

        memoryCache.addAll(data)

        //Notify change
        memoryCacheChangeListener.onNext(true)
    }
}