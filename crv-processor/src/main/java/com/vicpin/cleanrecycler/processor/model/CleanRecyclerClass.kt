package com.vicpin.cleanrecycler.processor.model

import com.vicpin.cleanrecyclerview.annotation.processor.entity.MapperClass
import com.vicpin.cleanrecyclerview.processor.util.EnvironmentUtil

/**
 * Created by victor on 23/4/18.
 */
class CleanRecyclerClass(val typeName: String, val cacheDataSource: DataSourceClass? = null, val cloudDataSource: DataSourceClass? = null, val mapper: MapperClass? = null) {

    fun getDataEntityTypeName() = when {
            cacheDataSource != null -> cacheDataSource.getDataEntityType()
            cloudDataSource != null -> cloudDataSource.getDataEntityType()
            else -> {
                EnvironmentUtil.logError("Neither cache datasource nor cloud datasource found for $typeName")
                ""
            }
        }

    fun getViewEntityTypeName(): String? = mapper?.getViewEntityType()

    fun hasCustomData() = cacheDataSource?.hasCustomData() == true || cloudDataSource?.hasCustomData() == true

    fun isPagedCloud() = cloudDataSource?.isPagedCloud() == true

    fun getCustomDataType() = cacheDataSource?.types?.get(1) ?: cloudDataSource?.types?.get(1)

    fun isCustomDataLanguajeType() = cacheDataSource?.isLanguajeType ?: cloudDataSource?.isLanguajeType ?: false


}