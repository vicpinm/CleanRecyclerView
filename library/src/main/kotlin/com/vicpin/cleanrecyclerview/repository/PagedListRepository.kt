package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudPagedDataSource
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Victor on 20/01/2017.
 */
class PagedListRepository<DataEntity, CustomData> constructor(internal var cache: CacheDataSource<DataEntity>, internal var cloud: CloudPagedDataSource<DataEntity, CustomData>? = null, var customData: CustomData? = null) : IRepository<DataEntity>  {

    override fun getData(currentPage: Int): Flowable<Pair<CRDataSource, List<DataEntity>>> {
        return if(cloud == null) {
            getDataFromDisk()
        }
        else if (currentPage == 0) {
            Flowable.concat(getDataFromDisk().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()),getDataPageFromCloud(currentPage).toFlowable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
        } else {
             getDataPageFromCloud(currentPage).toFlowable()
        }
    }

    override fun getDataFromDisk(): Flowable<Pair<CRDataSource, List<DataEntity>>> {
        return cache.getData().map { list ->
            Pair(CRDataSource.DISK, list)
        }
    }

    override fun getDataPageFromCloud(currentPage: Int): Single<Pair<CRDataSource, List<DataEntity>>> {
        return cloud!!.getData(currentPage, customData)
                .doOnSuccess { result ->
                    if (currentPage == 0) {
                        cache.clearData()
                    }
                }.doOnSuccess { data -> cache.saveData(data) }
                .map { list -> Pair(CRDataSource.CLOUD, list) }
    }

}
