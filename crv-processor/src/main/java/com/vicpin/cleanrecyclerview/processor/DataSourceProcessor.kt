package com.vicpin.cleanrecyclerview.processor



import com.vicpin.cleanrecyclerview.processor.model.Model
import com.vicpin.cleanrecyclerview.processor.util.EnvironmentUtil
import com.vicpin.cleanrecyclerview.processor.writter.CleanRecyclerWritter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Created by victor on 10/12/17.
 */
@SupportedAnnotationTypes("com.vicpin.cleanrecyclerview.annotation.DataSource","com.vicpin.cleanrecyclerview.annotation.Mapper")
class DataSourceProcessor : AbstractProcessor() {

    val cleanRecyclerWritter = CleanRecyclerWritter()


    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        EnvironmentUtil.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val model = buildModel(roundEnv)

        model.buildCleanRecyclerClasses().forEach {
            cleanRecyclerWritter.writeRecyclerClass(it, processingEnv)
        }

        return true
    }


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    private fun buildModel(env: RoundEnvironment): Model {
        return Model.buildFrom(env, supportedAnnotationTypes)
    }
}
