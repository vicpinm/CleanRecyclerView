package com.vicpin.cleanrecyclerview.repository.datasource

import io.reactivex.Single

/**
 * Created by Victor on 20/01/2017.
 */
interface ParamCloudDataSource<DataEntity, in CustomData>: CloudParamPagedDataSource<DataEntity, CustomData> {

    override fun getData(page: Int, data: CustomData?) = getData(data)

    fun getData(data: CustomData? = null): Single<List<DataEntity>>
}