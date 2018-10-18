package com.vicpin.cleanrecyclerview.repository

import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.ParamCacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.ParamCloudDataSource
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Victor on 20/01/2017.
 */
class ListRepository<DataEntity, CustomData> constructor(internal var cache: ParamCacheDataSource<DataEntity, CustomData>? = null, internal var cloud: ParamCloudDataSource<DataEntity, CustomData>? = null, var customData: CustomData? = null) : IRepository<DataEntity> {

    override fun getData(currentPage: Int): Flowable<Pair<CRDataSource, List<DataEntity>>> {
        return if (cloud != null && cache != null) {
            Flowable.merge(getDataFromDisk().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()), getDataPageFromCloud(currentPage, propagateErrors = false).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
        } else if (cache != null) {
            getDataFromDisk()
        } else {
            getDataPageFromCloud(currentPage, propagateErrors = true)
        }
    }

    override fun getDataFromDisk(): Flowable<Pair<CRDataSource, List<DataEntity>>> {
        return cache!!.getData(customData).map { list ->
            Pair(CRDataSource.DISK, list)
        }
    }


    override fun getDataPageFromCloud(currentPage: Int, propagateErrors: Boolean): Flowable<Pair<CRDataSource, List<DataEntity>>> {
        var result = cloud!!.getData(customData)
                .doOnSuccess { cache?.clearData() }
                .doOnSuccess { data -> cache?.saveData(data) }
                .map { data -> Pair(CRDataSource.CLOUD, data) }
                .toFlowable()

        if (!propagateErrors) {
            result = result.onErrorReturn { _: Throwable -> Pair(CRDataSource.CLOUD, listOf()) }
        }


        return result
    }

}
