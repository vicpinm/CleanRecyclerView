package com.vicpin.cleanrecyclerview.repository.datasource

import rx.Observable
import java.util.*

/**
 * Created by Oesia on 10/10/2017.
 */
class EmptyCache<T> : CacheDataSource<T> {

    override fun getData() = Observable.just(Collections.emptyList<T>())

    override fun clearData() {}

    override fun saveData(data: List<T>) {}
}
