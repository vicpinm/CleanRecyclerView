package com.vicpin.cleanrecyclerview.sample.data

import com.vicpin.cleanrecyclerview.annotation.DataSource
import com.vicpin.cleanrecyclerview.repository.datasource.SimpleCacheDataSource
import com.vicpin.cleanrecyclerview.sample.model.Item
import io.reactivex.Flowable

/**
 * Created by victor on 21/1/17.
 */
@DataSource
open class ItemCache: SimpleCacheDataSource<Item> {

    var memoryCache = mutableListOf<Item>()



    override fun getData() = Flowable.just(memoryCache.toList())

    override fun clearData() {
        memoryCache.clear()
    }

    override fun saveData(data: List<Item>) {
        memoryCache.addAll(data)
    }
}