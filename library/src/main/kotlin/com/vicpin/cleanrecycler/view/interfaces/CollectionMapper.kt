package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
abstract class CollectionMapper<ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {

    override var currentData = listOf<ViewEntity>()

    fun doTransform(newData: List<DataEntity>) : List<ViewEntity> {
        return transform(newData)
    }

    abstract fun transform(newData: List<DataEntity>) : List<ViewEntity>
}