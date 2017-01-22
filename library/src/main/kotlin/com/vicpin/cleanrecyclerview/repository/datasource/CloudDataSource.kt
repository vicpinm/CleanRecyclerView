package com.vicpin.cleanrecyclerview.repository.datasource

import rx.Observable

/**
 * Created by Victor on 20/01/2017.
 */
interface CloudDataSource<T> {

    fun getData(page: Int): Observable<List<T>>
}
