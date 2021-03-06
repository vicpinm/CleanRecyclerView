package com.vicpin.cleanrecycler.sample

import androidx.multidex.MultiDexApplication
import com.vicpin.cleanrecycler.sample.di.AppComponent
import com.vicpin.cleanrecycler.sample.di.AppModule
import com.vicpin.cleanrecycler.sample.di.DaggerAppComponent


/**
 * Created by Oesia on 29/05/2017.
 */

class Application : MultiDexApplication() {

    lateinit var applicationComponent: AppComponent

    companion object {
        lateinit var instance : Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDagger()
    }

    private fun initDagger() {
        applicationComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

    fun getAppComponent() = applicationComponent

    fun setAppComponent(component : AppComponent){
        this.applicationComponent = component
    }

}
