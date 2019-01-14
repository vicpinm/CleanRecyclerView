package com.vicpin.cleanrecycler.sample.model

import com.vicpin.cleanrecycler.view.interfaces.EntityMapper
import com.vicpin.cleanrecyclerview.annotation.Mapper

@Mapper
class Mapper: EntityMapper<Item, Item>() {
    override fun transform(dataEntity: Item): Item {
        return dataEntity
    }
}