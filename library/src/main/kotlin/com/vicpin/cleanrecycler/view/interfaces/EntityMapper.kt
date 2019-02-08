package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
abstract class EntityMapper<out ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {

    private var collection = mutableListOf<ViewEntity>()
    private var currentPosition = 0

    fun doTransform(position: Int, newData: DataEntity) : ViewEntity {
        currentPosition = position
        val result = transform(newData)

        if(position < collection.size) {
            collection[position] = result
        } else {
            collection.add(result)
        }

        return result
    }

    fun getCurrentData() = if(collection.size > currentPosition) collection[currentPosition] else null

    abstract fun transform(dataEntity: DataEntity) : ViewEntity
}