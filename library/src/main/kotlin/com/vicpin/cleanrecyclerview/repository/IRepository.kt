package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Oesia on 01/03/2017.
 */
interface IRepository<DataEntity> {
    fun getData(currentPage: Int): Flowable<Pair<CRDataSource, List<DataEntity>>>

    fun getDataFromDisk(): Flowable<Pair<CRDataSource, List<DataEntity>>>

    fun getDataPageFromCloud(currentPage: Int, propagateErrors: Boolean): Flowable<Pair<CRDataSource, List<DataEntity>>>

}
