package com.vicpin.cleanrecyclerview.sample.data

import android.util.Log
import com.vicpin.cleanrecyclerview.annotation.DataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.sample.model.Item
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

/**
 * Created by victor on 21/1/17.
 */
@DataSource
open class ItemCache: CacheDataSource<Item> {

    var memoryCache = mutableListOf<Item>()
    var subject = PublishSubject.create<Item>()


    override fun getData() = Flowable.create<List<Item>>({emmiter ->

        subject.subscribe {
            Log.e("aa","emitiendo subject ${memoryCache.size}")
            emmiter.onNext(memoryCache)

        }

        Log.e("aa","emitiendo ${memoryCache.size}")
        emmiter.onNext(memoryCache)


    }, BackpressureStrategy.DROP)

    override fun clearData() {
        Log.e("aa","cache clear")
        memoryCache.clear()
        subject.onNext(Item("","",""))
    }



    override fun saveData(data: List<Item>) {
        Log.e("aa","cache save ${data.size}")
        memoryCache.addAll(data)
        subject.onNext(Item("","",""))
    }
}