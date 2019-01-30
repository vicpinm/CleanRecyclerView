package com.vicpin.cleanrecycler.processor.writter

import java.io.File
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment

/**
 * Created by Victor on 26/01/2018.
 */
class FileWritter {

    companion object {
        private val KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"
        val PACKAGE: String = "com.vicpin.cleanrecycler"
    }

    var text = ""

    fun newLine(line: String = "", level: Int = 0, newLine: Boolean = false) {
        var indentation = ""
        var semicolon = if (!line.isEmpty() && !line.endsWith("}") && !line.endsWith("{")) "" else ""

        (1..level).forEach { indentation += "\t" }

        text += if (newLine) {
            "$indentation$line$semicolon\n\n"
        } else {
            "$indentation$line$semicolon\n"
        }
    }

    fun setPackage(packpage: String) {
        text = "package $packpage\n$text\n"
    }

    fun writeImport(line: String) {
        newLine(line)
    }

    fun openClass(line: String) {
        newLine()
        newLine(line + " {", newLine = true)
    }

    fun closeClass() {
        newLine("}")
    }

    fun openMethod(line: String) {
        newLine(line + " {", level = 1)
    }

    fun closeMethod() {
        newLine("}", level = 1, newLine = true)
    }


    fun methodBody(line: String, indentationLevel: Int = 0) {
        newLine(line, level = indentationLevel + 2)
    }

    fun generateFile(env: ProcessingEnvironment, className: String) {
        try { // write the env
            val options = env.options
            val kotlinGenerated = options[KAPT_KOTLIN_GENERATED_OPTION] ?: ""
            File(kotlinGenerated.replace("kaptKotlin","kapt"), "$className.kt").writer().buffered().use {
                it.appendln(text)
            }

        } catch (e: IOException) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }
    }



}