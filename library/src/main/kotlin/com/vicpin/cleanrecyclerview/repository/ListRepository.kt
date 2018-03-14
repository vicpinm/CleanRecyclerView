package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Victor on 20/01/2017.
 */
class ListRepository<T> constructor(internal var cache: CacheDataSource<T>, internal var cloud: CloudDataSource<T>? = null) : IRepository<T>  {

    override fun getData(currentPage: Int): Flowable<Pair<CRDataSource, List<T>>> {
        return if(cloud != null) {
            Flowable.concat(getDataFromDisk().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()), getDataPageFromCloud(currentPage).toFlowable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
        } else {
            getDataFromDisk()
        }
    }

    override fun getDataFromDisk(): Flowable<Pair<CRDataSource, List<T>>> {
        return cache.getData().map { list ->
            Pair<CRDataSource, List<T>>(CRDataSource.DISK, list)
        }
    }



    override fun getDataPageFromCloud(currentPage: Int): Single<Pair<CRDataSource, List<T>>> {
        return cloud!!.getData()
                .doOnSuccess { data -> cache.clearData() }
                .doOnSuccess { data -> cache.saveData(data) }
                .map { data -> Pair<CRDataSource, List<T>>(CRDataSource.CLOUD, data) }
    }
}
