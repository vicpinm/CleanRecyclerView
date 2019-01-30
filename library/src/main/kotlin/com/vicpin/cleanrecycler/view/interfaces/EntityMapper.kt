package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
abstract class EntityMapper<out ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {

    private var currentData: ViewEntity? = null

    fun doTransform(newData: DataEntity) : ViewEntity {
        val result = transform(newData)
        currentData = result
        return result
    }

    fun getCurrentData() = currentData

    abstract fun transform(dataEntity: DataEntity) : ViewEntity
}