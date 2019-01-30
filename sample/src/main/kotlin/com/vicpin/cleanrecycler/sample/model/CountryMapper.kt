package com.vicpin.cleanrecycler.sample.model

import com.vicpin.cleanrecycler.annotation.Mapper
import com.vicpin.cleanrecycler.view.interfaces.EntityMapper

@Mapper
open class CountryMapper: EntityMapper<Item, Country>() {
    val imageURL = "https://images-na.ssl-images-amazon.com/images/I/510SWSRqbrL._AC_UL130_.jpg"

    override fun transform(country: Country): Item {
        return Item(country.title, country.description, imageURL)
    }

}