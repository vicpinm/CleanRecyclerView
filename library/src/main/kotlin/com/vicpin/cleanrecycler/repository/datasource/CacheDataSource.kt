package com.vicpin.cleanrecycler.repository.datasource

import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
interface CacheDataSource<T>: ParamCacheDataSource<T,T> {

    override fun getData(data: T?): Flowable<List<T>> = getData()

    fun getData(): Flowable<List<T>>

}
