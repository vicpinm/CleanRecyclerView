package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface CloudPagedDataSource<T> {

    fun getData(page: Int): Single<List<T>>
}
