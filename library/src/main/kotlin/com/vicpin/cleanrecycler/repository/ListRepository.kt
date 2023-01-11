package com.vicpin.cleanrecycler.repository

import com.vicpin.cleanrecycler.repository.datasource.CloudParamPagedDataSource
import com.vicpin.cleanrecycler.repository.datasource.ParamCacheDataSource
import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
class ListRepository<DataEntity, CustomData> constructor(internal var cache: ParamCacheDataSource<DataEntity, CustomData>? = null, internal var cloud: CloudParamPagedDataSource<DataEntity, CustomData>? = null, var customData: CustomData? = null) {


    fun getDataFromDisk(): Flowable<List<DataEntity>> {
        return cache!!.getData(customData)
    }

    fun getDataPageFromCloud(currentPage: Int): Flowable<List<DataEntity>> {
        return cloud!!.getData(currentPage, customData)
                .doOnSuccess { data -> cache?.saveData(clearOldData = currentPage == 0, data = data) }
                .toFlowable()
    }

}
