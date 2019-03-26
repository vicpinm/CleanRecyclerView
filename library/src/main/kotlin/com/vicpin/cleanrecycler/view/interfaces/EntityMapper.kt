package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
abstract class EntityMapper<ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {

    override var currentData = listOf<ViewEntity>()

    private var currentPosition = 0

    fun doTransform(position: Int, newData: DataEntity) : ViewEntity {
        currentPosition = position
        return transform(newData)
    }

    fun getCurrentData() = if(currentData.size > currentPosition) currentData[currentPosition] else null

    abstract fun transform(dataEntity: DataEntity) : ViewEntity

}