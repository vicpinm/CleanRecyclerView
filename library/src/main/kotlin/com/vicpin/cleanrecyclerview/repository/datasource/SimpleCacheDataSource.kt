package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
interface SimpleCacheDataSource<T>: CacheDataSource<T,T> {

    override fun getData(data: T?): Flowable<List<T>> = getData()

    fun getData(): Flowable<List<T>>

}
