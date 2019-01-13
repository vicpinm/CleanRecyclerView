package com.vicpin.cleanrecyclerview.sample.model

import com.vicpin.cleanrecyclerview.annotation.Mapper
import com.vicpin.cleanrecyclerview.view.interfaces.EntityMapper

@Mapper
class Mapper: EntityMapper<Item, Item> {
    override fun transform(dataEntity: Item): Item {
        return dataEntity
    }
}