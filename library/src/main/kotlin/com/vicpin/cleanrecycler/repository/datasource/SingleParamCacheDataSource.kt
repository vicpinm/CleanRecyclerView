package com.vicpin.cleanrecycler.repository.datasource

import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface SingleParamCacheDataSource<DataEntity, in CustomData>: ParamCacheDataSource<DataEntity,CustomData> {

    override fun getData(data: CustomData?) : Flowable<List<DataEntity>> = getData().toFlowable()

    fun getData(): Single<List<DataEntity>>

}
