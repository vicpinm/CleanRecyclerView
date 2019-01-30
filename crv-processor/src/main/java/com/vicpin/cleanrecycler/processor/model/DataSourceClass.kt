package com.vicpin.cleanrecycler.processor.model

import com.vicpin.cleanrecycler.processor.model.DataSourceClass.DataSourceParent.*
import com.vicpin.cleanrecycler.processor.util.EnvironmentUtil
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * Created by victor on 15/12/17.
 */
class DataSourceClass(val annotatedField: Element) {

    enum class DataSourceParent(val className: String) {
        CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.CloudDataSource"),
        PAGED_CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.CloudPagedDataSource"),
        CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.CacheDataSource"),
        CLOUD_PARAM_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.CloudParamDataSource"),
        CLOUD_PARAM_PAGED_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.CloudParamPagedDataSource"),
        PARAM_CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.ParamCacheDataSource"),
        SINGLE_CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.SingleCacheDataSource"),
        SINGLE_PARAM_CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecycler.repository.datasource.SingleParamCacheDataSource");

        companion object {
            fun fromClassName(className: String) = DataSourceParent.values().first { it.className == className }
        }
    }

    val classReference: String
    lateinit var parentClass: DataSourceParent
    var types = listOf<String>()
    var isLanguajeType = false

    init {
        classReference = annotatedField.toString()
        getParentClassInfo()
    }


    fun getParentClassInfo()  {
        if (annotatedField is TypeElement) {
            val te = annotatedField

            for (typeMirror in te.interfaces) {
                if (typeMirror is DeclaredType) {
                    parentClass = DataSourceParent.fromClassName(typeMirror.asElement().toString())

                    types = typeMirror.typeArguments.toList().map { it.toString() }
                    isLanguajeType = EnvironmentUtil.isBasicType(typeMirror.typeArguments.getOrNull(1))
                }
            }
        }
    }

    fun getDataEntityType() = types.first()

    fun isCacheDataSource() = parentClass == CACHE_DATASOURCE_CLASS || parentClass == PARAM_CACHE_DATASOURCE_CLASS || parentClass == SINGLE_CACHE_DATASOURCE_CLASS || parentClass == SINGLE_PARAM_CACHE_DATASOURCE_CLASS

    fun isCloudDataSource() = !isCacheDataSource()

    fun isPagedCloud() = parentClass == PAGED_CLOUD_DATASOURCE_CLASS || parentClass == CLOUD_PARAM_PAGED_DATASOURCE_CLASS

    fun hasCustomData() = listOf(CLOUD_PARAM_DATASOURCE_CLASS, CLOUD_PARAM_PAGED_DATASOURCE_CLASS, PARAM_CACHE_DATASOURCE_CLASS, SINGLE_PARAM_CACHE_DATASOURCE_CLASS).contains(parentClass)

}


