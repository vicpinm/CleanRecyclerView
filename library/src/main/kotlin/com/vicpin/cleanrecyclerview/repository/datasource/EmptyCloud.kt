package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Single
import java.util.*

/**
 * Created by Oesia on 10/10/2017.
 */
class EmptyCloud<T> : CloudDataSource<T, T> {

    override fun getData(data: T?): Single<List<T>> {
        return Single.just(Collections.emptyList())
    }
}