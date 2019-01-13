package com.vicpin.cleanrecycler.repository.datasource

import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface SingleCacheDataSource<T>: SingleParamCacheDataSource<T, T> {

    override fun getData(data: T?) : Flowable<List<T>> = getData().toFlowable()


}
