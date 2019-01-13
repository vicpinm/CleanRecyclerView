package com.vicpin.cleanrecycler.sample.data

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.vicpin.cleanrecycler.repository.datasource.CacheDataSource
import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.cleanrecyclerview.annotation.DataSource

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

            Handler(Looper.getMainLooper()).postDelayed({
                if(memoryCache.size > 0) {
                    memoryCache.removeAt(memoryCache.size - 1)
                    subject.onNext(Item("","",""))

                }
            },3000)

        }

        Log.e("aa","emitiendo ${memoryCache.size}")
        emmiter.onNext(memoryCache)


    }, BackpressureStrategy.DROP)

    private fun clearData() {
        Log.e("aa","cache clear")
        memoryCache.clear()
    }



    override fun saveData(clearOldData: Boolean, data: List<Item>) {
        if(clearOldData) {
            clearData()
        }

        Log.e("aaa","cache save ${data.size}")
        memoryCache.addAll(data)
        subject.onNext(Item("","",""))
    }
}