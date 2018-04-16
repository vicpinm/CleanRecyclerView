package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface SimpleCloudPagedDataSource<T>: CloudPagedDataSource<T,T> {

    override fun getData(page: Int, data: T?): Single<List<T>> = getData(page)

    fun getData(page: Int): Single<List<T>>
}
