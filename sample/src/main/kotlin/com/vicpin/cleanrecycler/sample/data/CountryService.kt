package com.vicpin.cleanrecycler.sample.data

import com.vicpin.cleanrecycler.annotation.DataSource
import com.vicpin.cleanrecycler.annotation.Mapper
import com.vicpin.cleanrecycler.repository.datasource.CloudDataSource
import com.vicpin.cleanrecycler.sample.model.Country
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@DataSource
open class CountryService: CloudDataSource<Country> {

    val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."


    override fun getData(): Single<List<Country>> {
        return Single.just(getFakeItems()).delay(2, TimeUnit.SECONDS)
    }

    fun getFakeItems(): List<Country> {

        val list = mutableListOf<Country>()
        val item1 = Country("Country card 1", description)
        val item2 = Country("Country card 2", description)
        val item3 = Country("Country card 3", description)
        val item4 = Country("Country card 4", description)
        val item5 = Country("Country card 5", description)

        list.add(item1)
        list.add(item2)
        list.add(item3)
        list.add(item4)
        list.add(item5)

        return list
    }
}