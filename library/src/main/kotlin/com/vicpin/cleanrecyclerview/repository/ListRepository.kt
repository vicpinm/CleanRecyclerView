package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import rx.Observable

/**
 * Created by Victor on 20/01/2017.
 */
class ListRepository<T> constructor(internal var cache: CacheDataSource<T>, internal var cloud: CloudDataSource<T>) : IRepository<T>  {

    override fun getData(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        return getDataFromDisk().mergeWith(getDataPageFromCloud(currentPage))
    }

    override fun getDataFromDisk(): Observable<Pair<CRDataSource, List<T>>> {
        return cache.data.map { list ->
            Pair<CRDataSource, List<T>>(CRDataSource.DISK, list)
        }
    }

    override fun getDataPageFromCloud(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        return cloud.getData()
                .doOnNext { result -> cache.clearData() }
                .doOnNext { data -> cache.saveData(data) }
                .map { list -> Pair<CRDataSource, List<T>>(CRDataSource.CLOUD, list) }
    }

}
