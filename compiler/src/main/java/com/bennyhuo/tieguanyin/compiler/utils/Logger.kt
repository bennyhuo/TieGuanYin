package com.bennyhuo.tieguanyin.compiler.utils

import java.io.PrintWriter
import java.io.StringWriter

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * Created by benny on 2/3/18.
 */
object Logger {

    lateinit var messager: Messager

    fun debug(message: String) {
        messager.printMessage(Diagnostic.Kind.OTHER, message)
    }

    fun error(element: Element, message: String, vararg args: Any) {
        printMessage(Diagnostic.Kind.ERROR, element, message, *args)
    }

    fun note(element: Element, message: String, vararg args: Any) {
        printMessage(Diagnostic.Kind.NOTE, element, message, *args)
    }

    fun logParsingError(element: Element, annotation: Class<out Annotation>, e: Exception) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.simpleName, stackTrace)
    }

    private fun printMessage(kind: Diagnostic.Kind, element: Element, message: String, vararg args: Any) {
        messager.printMessage(kind,
                if (args.isNotEmpty()) { String.format(message, *args) } else message
                , element)
    }
}
