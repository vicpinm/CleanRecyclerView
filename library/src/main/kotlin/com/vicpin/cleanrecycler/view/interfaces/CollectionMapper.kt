package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
abstract class CollectionMapper<ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {

    private var currentData: List<ViewEntity>? = null

    fun doTransform(newData: List<DataEntity>) : List<ViewEntity> {
        val result = transform(newData)
        currentData = result
        return result
    }

    fun getCurrentData() = currentData

    abstract fun transform(newData: List<DataEntity>) : List<ViewEntity>
}