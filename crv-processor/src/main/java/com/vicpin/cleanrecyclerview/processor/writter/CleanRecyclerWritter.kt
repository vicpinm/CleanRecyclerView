package com.vicpin.cleanrecyclerview.processor.writter

import com.vicpin.cleanrecyclerview.annotation.processor.entity.MapperClass
import com.vicpin.cleanrecyclerview.processor.model.CleanRecyclerClass
import javax.annotation.processing.ProcessingEnvironment


/**
 * Created by victor on 10/12/17.
 */
class CleanRecyclerWritter {

    private val writter = FileWritter()
    private val CLEAN_RECYCLER_CLASS = "CleanRecyclerView"

    var className = ""
    var parentClassName = ""
    var viewEntityName = ""
    var dataEntityName = ""

    fun createPackage(packpage: String) {
        writter.setPackage(packpage)
    }

    fun generateImports(model: CleanRecyclerClass) {
        writter.writeImport("import android.content.Context")
        writter.writeImport("import android.content.Intent")
        writter.writeImport("import com.vicpin.cleanrecyclerview.view.CleanRecyclerView")
        writter.writeImport("import android.util.AttributeSet")
        writter.writeImport("import com.vicpin.kpresenteradapter.PresenterAdapter")
        if(viewEntityName.isNotBlank()) writter.writeImport("import $viewEntityName")
        if(dataEntityName.isNotBlank() && viewEntityName != dataEntityName) writter.writeImport("import $dataEntityName")
        if(model.cacheDataSource != null) writter.writeImport("import ${model.cacheDataSource.classReference}")
        if(model.cloudDataSource != null) writter.writeImport("import ${model.cloudDataSource.classReference}")
        if(model.hasCustomData() && !model.isCustomDataLanguajeType()) writter.writeImport("import ${model.getCustomDataType()}")
        if(model.mapper != null) writter.writeImport("import ${model.mapper.classReference}")
    }

    fun generateClass() {
        writter.openClass("class $className: $parentClassName")
    }

    fun generateConstructors() {
        writter.newLine("constructor(context: Context?) : super(context)", level = 1)
        writter.newLine("constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)", level = 1)
        writter.newLine("constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)", level = 1, newLine = true)
    }

    fun generateLoadMethod(model: CleanRecyclerClass) {

        val adapterEntity = (if(model.getViewEntityTypeName() != null) model.getViewEntityTypeName() else model.getDataEntityTypeName())?.simpleTypeName()

        val paged = model.cloudDataSource?.isPagedCloud() ?: false

        var customDataLoadFunctionArgument = ""
        var customDataParameter: String

        if(model.hasCustomData()) {
            val customDataType = model.getCustomDataType()?.simpleTypeName()
            customDataLoadFunctionArgument = ", customData: $customDataType? = null"
            customDataParameter = "customData = customData"
        } else {
            customDataParameter = "customData = null"
        }

        writter.openMethod("fun load(adapter: PresenterAdapter<$adapterEntity>$customDataLoadFunctionArgument)")
        writter.methodBody((if(paged) "super.loadPaged" else "super.load") + ("(adapter = adapter, cloud = ${if(model.cloudDataSource != null) "${model.cloudDataSource.classReference.simpleTypeName()}::class" else "null"}, cache = ${if(model.cacheDataSource != null) "${model.cacheDataSource.classReference.simpleTypeName()}::class" else "null"}, mapper = ${if(model.mapper != null) "${model.mapper.classReference.simpleTypeName()}::class.java.newInstance()" else "null"}, $customDataParameter)"))
        writter.closeMethod()
    }


    fun closeClass() {
        writter.closeClass()
    }


    fun writeRecyclerClass(model: CleanRecyclerClass, processingEnv: ProcessingEnvironment) {

        viewEntityName = model.getViewEntityTypeName() ?: ""
        dataEntityName = model.getDataEntityTypeName()

        val simpleDataEntityName = dataEntityName.simpleTypeName()

        className = (if(dataEntityName.isNotBlank()) simpleDataEntityName else simpleDataEntityName) + CLEAN_RECYCLER_CLASS
        parentClassName = if(model.mapper != null) getParentClassNameFromMapper(model.mapper) else getParentClassNameFromViewType(simpleDataEntityName)

        createPackage(FileWritter.PACKAGE)
        generateImports(model)

        generateClass()
        generateConstructors()

        generateLoadMethod(model)


        closeClass()
        writter.generateFile(processingEnv, className)

    }

    private fun getParentClassNameFromMapper(mapper: MapperClass): String {
        val dataEntityType = mapper.getDataEntityType() ?: mapper.getViewEntityType()
        return "$CLEAN_RECYCLER_CLASS<${mapper.getViewEntityType().simpleTypeName()},${dataEntityType.simpleTypeName()}>"
    }

    private fun getParentClassNameFromViewType(viewEntityType: String): String {
        return "$CLEAN_RECYCLER_CLASS<$viewEntityType,$viewEntityType>"
    }


}

fun String.simpleTypeName() = substringAfterLast(".")
