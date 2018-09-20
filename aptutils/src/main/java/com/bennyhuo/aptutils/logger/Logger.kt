package com.bennyhuo.aptutils.logger

import com.bennyhuo.aptutils.AptContext
import java.io.PrintWriter
import java.io.StringWriter
import javax.lang.model.element.Element
import javax.tools.Diagnostic
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE

/**
 * Created by benny on 2/3/18.
 */
object Logger {

    fun debug(message: String) {
        AptContext.messager.printMessage(Diagnostic.Kind.OTHER, message)
    }

    fun error(element: Element, message: String, vararg args: Any) {
        printMessage(ERROR, element, message, *args)
    }

    fun note(element: Element, message: String, vararg args: Any) {
        printMessage(NOTE, element, message, *args)
    }

    fun logParsingError(element: Element, annotation: Class<out Annotation>, e: Exception) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.simpleName, stackTrace)
    }

    private fun printMessage(kind: Diagnostic.Kind, element: Element, message: String, vararg args: Any) {
        AptContext.messager.printMessage(kind,
                if (args.isNotEmpty()) { String.format(message, *args) } else message
                , element)
    }
}
