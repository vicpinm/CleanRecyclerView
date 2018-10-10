package com.vicpin.cleanrecyclerview.sample.data

import com.vicpin.cleanrecyclerview.annotation.DataSource
import com.vicpin.cleanrecyclerview.repository.datasource.SimpleCacheDataSource
import com.vicpin.cleanrecyclerview.sample.model.Item
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/**
 * Created by victor on 21/1/17.
 */
@DataSource
open class ItemCache: SimpleCacheDataSource<Item> {

    var memoryCache = getFakeItems()


    override fun getData() = Flowable.create<List<Item>>({emmiter ->

        repeat(10) {
            memoryCache = memoryCache.dropLast(1)
            Thread.sleep(2000)
            emmiter.onNext(memoryCache)
        }

    }, BackpressureStrategy.DROP)

    override fun clearData() {
    }

    fun getFakeItems(): List<Item> {

        val list = mutableListOf<Item>()
        val item1 = Item("Material card 1", "", "https://s-media-cache-ak0.pinimg.com/736x/c4/30/40/c4304020ba04dfc3ad212e94515fe1f4.jpg")
        val item2 = Item("Material card 2", "", "https://lh3.googleusercontent.com/-JjdCDI4-CJg/V0v8Tk3ng7I/AAAAAAABq7o/6fRDNinKmzoURluAg29-hp8LPysvUc_PA/w800-h800/Material-Design-3.jpg")
        val item3 = Item("Material card 3", "", "https://s-media-cache-ak0.pinimg.com/originals/56/38/8a/56388af0c9e5a88342e425baa50a95a6.png")
        val item4 = Item("Material card 4", "", "https://lh3.googleusercontent.com/-20FtHlNUVXI/V0v8TYatATI/AAAAAAABq7o/xRSUz_W7OOMhmSxKZDBUUf-U6rxKllyhQ/w800-h800/Material-Design-2.jpg")
        val item5 = Item("Material card 5", "", "https://lh3.googleusercontent.com/-SqJb4HuQr5k/V0pvoU_a1nI/AAAAAAAA6-0/0MbAFl2sxigt3PtYYGhE6VJeC_7KHsbfA/w960-h540/%2540OsumWalls%2BMaterial%2B%2BWallpapers%2B%25281%2529.jpg")
        val item6 = Item("Material card 5", "", "https://lh3.googleusercontent.com/-SqJb4HuQr5k/V0pvoU_a1nI/AAAAAAAA6-0/0MbAFl2sxigt3PtYYGhE6VJeC_7KHsbfA/w960-h540/%2540OsumWalls%2BMaterial%2B%2BWallpapers%2B%25281%2529.jpg")
        val item7= Item("Material card 5", "", "https://lh3.googleusercontent.com/-SqJb4HuQr5k/V0pvoU_a1nI/AAAAAAAA6-0/0MbAFl2sxigt3PtYYGhE6VJeC_7KHsbfA/w960-h540/%2540OsumWalls%2BMaterial%2B%2BWallpapers%2B%25281%2529.jpg")
        val item8 = Item("Material card 5", "", "https://lh3.googleusercontent.com/-SqJb4HuQr5k/V0pvoU_a1nI/AAAAAAAA6-0/0MbAFl2sxigt3PtYYGhE6VJeC_7KHsbfA/w960-h540/%2540OsumWalls%2BMaterial%2B%2BWallpapers%2B%25281%2529.jpg")

        list.add(item1)
        list.add(item2)
        list.add(item3)
        list.add(item4)
        list.add(item5)
        list.add(item6)
        list.add(item7)
        list.add(item8)

        return list
    }

    override fun saveData(data: List<Item>) {
    }
}