package com.vicpin.cleanrecycler.annotation.processor.entity

import com.vicpin.cleanrecycler.annotation.Mapper
import com.vicpin.cleanrecycler.processor.util.EnvironmentUtil
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * Created by victor on 15/12/17.
 */
@Mapper
class MapperClass(val annotatedField: Element) {

    enum class MapperParent(val className: String) {
        ENTITY_MAPPER_CLASS("com.vicpin.cleanrecycler.view.interfaces.EntityMapper"),
        COLLECTION_MAPPER_CLASS("com.vicpin.cleanrecycler.view.interfaces.CollectionMapper");

        companion object {
            fun fromClassName(className: String) = MapperParent.values().first { it.className == className }
        }
    }

    val classReference: String
    lateinit var parentClass: MapperParent
    var types = listOf<String>()

    init {

        classReference = annotatedField.toString()
        getParentClassInfo()

    }


    fun getParentClassInfo() {

        if (annotatedField is TypeElement) {
            val te = annotatedField
            val parent = te.superclass

            if (parent is DeclaredType) {
                parentClass = MapperParent.fromClassName(parent.asElement().toString())
                types = parent.typeArguments.toList().map { it.toString() }
            }

        }
    }

    fun getViewEntityType() = types[0]
    fun getDataEntityType() = if (types.size > 1) types[1] else null


}


