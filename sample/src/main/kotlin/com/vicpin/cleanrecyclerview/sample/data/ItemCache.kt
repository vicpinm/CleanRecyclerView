package com.vicpin.cleanrecyclerview.sample.data

import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.sample.model.Item
import rx.Observable

/**
 * Created by victor on 21/1/17.
 */
class ItemCache : CacheDataSource<Item>{

    var memoryCache = mutableListOf<Item>()


    override fun getData() = Observable.just(memoryCache.toList())

    override fun clearData() {
        memoryCache.clear()
    }

    override fun saveData(data: List<Item>) {
        memoryCache.addAll(data)
    }
}