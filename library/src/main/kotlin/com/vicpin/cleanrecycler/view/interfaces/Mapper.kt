package com.vicpin.cleanrecycler.view.interfaces

/**
 * Created by Oesia on 22/01/2018.
 */
interface Mapper<ViewEntity, in DataEntity> {

    var currentData: List<ViewEntity>

}