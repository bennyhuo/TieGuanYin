package com.bennyhuo.aptutils.logger

import com.bennyhuo.aptutils.AptContext.messager
import java.io.PrintWriter
import java.io.StringWriter
import javax.lang.model.element.Element
import javax.tools.Diagnostic
import javax.tools.Diagnostic.Kind.*

/**
 * Created by benny on 2/3/18.
 */
object Logger {

    fun warn(element: Element, message: String, vararg args: Any) {
        printMessage(WARNING, element, message, *args)
    }

    fun warn(message: String, vararg args: Any) {
        printMessage(WARNING, null, message, *args)
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

    private fun printMessage(kind: Diagnostic.Kind, element: Element?, message: String, vararg args: Any) {
        if(element == null){
            messager.printMessage(Diagnostic.Kind.WARNING, if (args.isNotEmpty()) { String.format(message, *args) } else message)
        } else {
            messager.printMessage(kind,
                    if (args.isNotEmpty()) {
                        String.format(message, *args)
                    } else message
                    , element)
        }
    }
}
