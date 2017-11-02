package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Oesia on 01/03/2017.
 */
interface IRepository<T> {
    fun getData(currentPage: Int): Flowable<Pair<CRDataSource, List<T>>>

    fun getDataFromDisk(): Flowable<Pair<CRDataSource, List<T>>>

    fun getDataPageFromCloud(currentPage: Int): Single<Pair<CRDataSource, List<T>>>

}
