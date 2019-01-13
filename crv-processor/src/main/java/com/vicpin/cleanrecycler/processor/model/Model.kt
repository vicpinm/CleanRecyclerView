package com.vicpin.cleanrecycler.processor.model


import com.vicpin.cleanrecyclerview.annotation.DataSource
import com.vicpin.cleanrecyclerview.annotation.Mapper
import com.vicpin.cleanrecyclerview.annotation.processor.entity.MapperClass
import com.vicpin.cleanrecyclerview.processor.util.EnvironmentUtil
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

/**
 * Created by Victor on 07/12/2017.
 */
class Model private constructor() {

    var dataSourceClasses = mutableListOf<DataSourceClass>()
    var mapperClasses = mutableListOf<MapperClass>()


    private fun parseAnnotation(env: RoundEnvironment, annotationClass: Class<Annotation>) {
        val annotatedClasses = env.getElementsAnnotatedWith(annotationClass)

        for (annotatedClass in annotatedClasses) {
            parseAnnotatedClass(annotatedClass, annotationClass)
        }

    }


    fun parseAnnotatedClass(annotatedClass: Element, annotationClass: Class<Annotation>) {
        if (annotatedClass.getAnnotation(DataSource::class.java) != null) {

            if (annotatedClass.kind != ElementKind.CLASS) {
                EnvironmentUtil.logError(annotationClass.simpleName + " can only be used for classes")
                return
            }

            val property = DataSourceClass(annotatedClass)

            dataSourceClasses.add(property)

        } else if (annotatedClass.getAnnotation(Mapper::class.java) != null) {

            if (annotatedClass.kind != ElementKind.CLASS) {
                EnvironmentUtil.logError(annotationClass.simpleName + " can only be used for classes")
                return
            }


            val property = MapperClass(annotatedClass)

            mapperClasses.add(property)
        }
    }

    fun buildCleanRecyclerClasses(): List<CleanRecyclerClass> {
        var result = mutableListOf<CleanRecyclerClass>()

        var types = dataSourceClasses.distinctBy { it.getDataEntityType() }.map { it.getDataEntityType() }

        types.forEach { type ->


            val dataSourcesForType = dataSourceClasses.filter { it.getDataEntityType() == type }
            val mappersForType = mapperClasses.filter { it.getDataEntityType() == type }

            if(dataSourcesForType.size > 2) {
                EnvironmentUtil.logError("Too many datasource classes found (${dataSourcesForType.size} for type $type")
            }

            if(mappersForType.size > 1) {
                EnvironmentUtil.logError("More than one mapper classes found (${mappersForType.size} for type $type")
            }

            val cleanRecyclerClass = CleanRecyclerClass(typeName = type,
                    cacheDataSource = dataSourcesForType.firstOrNull{ it.isCacheDataSource() },
                    cloudDataSource = dataSourcesForType.firstOrNull{ it.isCloudDataSource() },
                    mapper = mappersForType.firstOrNull())

            result.add(cleanRecyclerClass)

            if(cleanRecyclerClass.cacheDataSource != null && cleanRecyclerClass.cloudDataSource != null) {
                val customDataInCache = cleanRecyclerClass.cacheDataSource.hasCustomData()
                val customDataInCloud = cleanRecyclerClass.cloudDataSource.hasCustomData()

                if(customDataInCache != customDataInCloud) {
                    EnvironmentUtil.logError("If you use custom data in your datasource for a given type, the other datasource must receive custom data too. Cache data source class: ${cleanRecyclerClass.cacheDataSource.classReference}, cloud data source class: ${cleanRecyclerClass.cloudDataSource.classReference} ")
                } else if(customDataInCache && customDataInCloud) {

                    if(cleanRecyclerClass.cacheDataSource.types[1] != cleanRecyclerClass.cloudDataSource.types[1]) {
                        EnvironmentUtil.logError("Your datasources does not share the same custom data type. Cache data source class: ${cleanRecyclerClass.cacheDataSource.classReference}, cloud data source class: ${cleanRecyclerClass.cloudDataSource.classReference} ")
                    }
                }

            }




        }

        return result
    }


    companion object {

        fun buildFrom(env: RoundEnvironment, annotations: Set<String>): Model {
            val model = Model()

            for (annotation in annotations) {
                try {
                    model.parseAnnotation(env, Class.forName(annotation) as Class<Annotation>)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            return model
        }
    }
}


