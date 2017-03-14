package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudPagedDataSource
import rx.Observable

/**
 * Created by Victor on 20/01/2017.
 */
class PagedListRepository<T> constructor(internal var cache: CacheDataSource<T>, internal var cloud: CloudPagedDataSource<T>) : IRepository<T>  {

    override fun getData(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        if (currentPage == 0) {
            return getDataFromDisk().mergeWith(getDataPageFromCloud(currentPage))
        } else {
            return getDataPageFromCloud(currentPage)
        }
    }

    override fun getDataFromDisk(): Observable<Pair<CRDataSource, List<T>>> {
        return cache.getData().map { list ->
            Pair<CRDataSource, List<T>>(CRDataSource.DISK, list)
        }
    }

    override fun getDataPageFromCloud(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        return cloud.getData(currentPage)
                .doOnNext { result ->
                    if (currentPage == 0) {
                        cache.clearData()
                    }
                }.doOnNext { data -> cache.saveData(data) }
                .map { list -> Pair<CRDataSource, List<T>>(CRDataSource.CLOUD, list) }
    }

}
