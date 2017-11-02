package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
interface CacheDataSource<T> {

    fun getData() : Flowable<List<T>>

    fun clearData()

    fun saveData(data: List<T>)

}
