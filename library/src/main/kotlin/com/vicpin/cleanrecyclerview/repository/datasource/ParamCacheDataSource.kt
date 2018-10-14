package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
interface ParamCacheDataSource<DataEntity, in CustomData> {

    fun getData(data: CustomData? = null) : Flowable<List<DataEntity>>

    fun clearData()

    fun saveData(data: List<DataEntity>)

}
