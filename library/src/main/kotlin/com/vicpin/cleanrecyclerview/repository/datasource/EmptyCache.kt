package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Flowable
import java.util.*

/**
 * Created by Oesia on 10/10/2017.
 */
class EmptyCache<T> : CacheDataSource<T> {

    override fun getData() = Flowable.just(Collections.emptyList<T>())

    override fun clearData() {}

    override fun saveData(data: List<T>) {}
}
