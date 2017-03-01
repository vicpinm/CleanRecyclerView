package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import rx.Observable

/**
 * Created by Oesia on 01/03/2017.
 */
interface IRepository<T> {
    fun getData(currentPage: Int): Observable<Pair<CRDataSource, List<T>>>

    fun getDataFromDisk(): Observable<Pair<CRDataSource, List<T>>>

    fun getDataPageFromCloud(currentPage: Int): Observable<Pair<CRDataSource, List<T>>>

}
