package com.vicpin.cleanrecycler.processor.util

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * Created by victor on 10/12/17.
 */
object EnvironmentUtil {
    private var processingEnvironment: ProcessingEnvironment? = null
    var utils: Elements? = null
        private set

    fun init(environment: ProcessingEnvironment) {
        processingEnvironment = environment
        utils = processingEnvironment?.elementUtils
    }

    fun logError(message: String) {
        processingEnvironment?.messager?.printMessage(Diagnostic.Kind.ERROR, message)
    }

    fun logWarning(message: String) {
        processingEnvironment?.messager?.printMessage(Diagnostic.Kind.WARNING, message)
    }

    @Throws(IOException::class)
    fun generateFile(typeSpec: TypeSpec, packageName: String) {
        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(processingEnvironment?.filer)
    }

    fun isSerializable(typeMirror: TypeMirror): Boolean {
        val serializable = processingEnvironment!!.elementUtils
                .getTypeElement("java.io.Serializable").asType()
        return processingEnvironment?.typeUtils?.isAssignable(typeMirror, serializable) ?: false
    }

    fun isParcelable(typeMirror: TypeMirror): Boolean {
        val parcelable = processingEnvironment!!.elementUtils
                .getTypeElement("android.os.Parcelable").asType()
        return processingEnvironment?.typeUtils?.isAssignable(typeMirror, parcelable) ?: false
    }


    fun isParcelableArray(type: TypeMirror): Boolean {
        return if (!type.toString().contains("java.util.ArrayList")) {
            false
        } else if (type is DeclaredType && type.typeArguments.isNotEmpty()) {
            val typeArgument = type.typeArguments[0]
            val isValid = isParcelable(typeArgument)
            if (!isValid) {
                logError("Found annotated list field with type $type whose argument type $typeArgument does not implements Parcelable")
            }
            return isValid
        } else {
            false
        }
    }

    fun isParcelableWithParceler(type: TypeMirror): Boolean {
        var finalType = type
        if (type.toString().contains("java.util.ArrayList") && type is DeclaredType && type.typeArguments.isNotEmpty()) {
            finalType = type.typeArguments[0]
        }

        utils?.getTypeElement(finalType.toString())?.let {
            return it.annotationMirrors?.any {
                it.toString().contains("org.parceler.Parcel")
            } ?: false
        }
        return false
    }

    fun isArray(typeMirror: TypeMirror): Boolean {
        val array = processingEnvironment!!.elementUtils
                .getTypeElement("").asType()
        return false
    }

    fun isBasicType(type: TypeMirror?) = type?.kind?.isPrimitive ?: false || type?.toString() == String::class.java.name

}
