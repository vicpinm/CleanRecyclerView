package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
interface EntityMapper<out ViewEntity, in DataEntity>: Mapper<ViewEntity, DataEntity> {
    fun transform(dataEntity: DataEntity) : ViewEntity
}