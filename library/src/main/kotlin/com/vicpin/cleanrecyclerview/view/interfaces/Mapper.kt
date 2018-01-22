package com.vicpin.cleanrecyclerview.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
interface Mapper<out ViewEntity, in DataEntity> {
    fun transform(dataEntity: DataEntity) : ViewEntity
}