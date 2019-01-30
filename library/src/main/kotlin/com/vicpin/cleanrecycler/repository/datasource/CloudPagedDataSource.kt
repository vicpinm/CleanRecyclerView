package com.vicpin.cleanrecycler.repository.datasource

import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface CloudPagedDataSource<T>: CloudParamPagedDataSource<T,T> {

    override fun getData(page: Int, data: T?): Single<List<T>> = getData(page)

    fun getData(page: Int): Single<List<T>>
}
