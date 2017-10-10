package com.vicpin.cleanrecyclerview.repository.datasource

import rx.Observable

/**
 * Created by Oesia on 10/10/2017.
 */
class EmptyCloud<T> : CloudDataSource<T> {

    override fun getData(): Observable<List<T>> {
        return Observable.empty()
    }
}