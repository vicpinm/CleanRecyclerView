package com.vicpin.cleanrecycler.sample.model

import com.vicpin.cleanrecyclerview.annotation.Mapper
import com.vicpin.cleanrecycler.view.interfaces.EntityMapper

@Mapper
class Mapper: EntityMapper<Item, Item> {
    override fun transform(dataEntity: Item): Item {
        return dataEntity
    }
}