package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

/**
 * Created by Victor on 20/01/2017.
 */
class ListRepository<T> constructor(internal var cache: CacheDataSource<T>, internal var cloud: CloudDataSource<T>) : IRepository<T>  {

    override fun getData(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        return Observable.concat(getDataFromDisk().observeOn(AndroidSchedulers.mainThread()),getDataPageFromCloud(currentPage).observeOn(AndroidSchedulers.mainThread()))
    }

    override fun getDataFromDisk(): Observable<Pair<CRDataSource, List<T>>> {
        return cache.getData().map { list ->
            Pair<CRDataSource, List<T>>(CRDataSource.DISK, list)
        }
    }

    override fun getDataPageFromCloud(currentPage: Int): Observable<Pair<CRDataSource, List<T>>> {
        return cloud.getData()
                .doOnNext { data -> cache.clearData() }
                .doOnNext { data -> cache.saveData(data) }
                .map { data -> Pair<CRDataSource, List<T>>(CRDataSource.CLOUD, data) }
    }

}
