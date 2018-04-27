package com.vicpin.cleanrecyclerview.processor.model

import com.vicpin.cleanrecyclerview.processor.model.DataSourceClass.DataSourceParent.*
import com.vicpin.cleanrecyclerview.processor.util.EnvironmentUtil
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * Created by victor on 15/12/17.
 */
class DataSourceClass(val annotatedField: Element) {

    enum class DataSourceParent(val className: String) {
        CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource"),
        SIMPLE_CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.SimpleCloudDataSource"),
        PAGED_CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.CloudPagedDataSource"),
        SIMPLE_DATA_CLOUD_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.SimpleCloudPagedDataSource"),
        CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource"),
        SIMPLE_CACHE_DATASOURCE_CLASS("com.vicpin.cleanrecyclerview.repository.datasource.SimpleCacheDataSource");

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

    fun isCacheDataSource() = parentClass == CACHE_DATASOURCE_CLASS || parentClass == SIMPLE_CACHE_DATASOURCE_CLASS

    fun isCloudDataSource() = !isCacheDataSource()

    fun isPagedCloud() = parentClass == PAGED_CLOUD_DATASOURCE_CLASS || parentClass == SIMPLE_DATA_CLOUD_DATASOURCE_CLASS

    fun hasCustomData() = listOf(PAGED_CLOUD_DATASOURCE_CLASS, CLOUD_DATASOURCE_CLASS, CACHE_DATASOURCE_CLASS).contains(parentClass)

}


