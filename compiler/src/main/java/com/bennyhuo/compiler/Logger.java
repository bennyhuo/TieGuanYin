package com.bennyhuo.compiler;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Created by benny on 2/3/18.
 */

public class Logger {

    public static Messager messager;

    public static void debug(String message){
        messager.printMessage(Diagnostic.Kind.OTHER, message);
    }

    public static void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    public static void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private static void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        messager.printMessage(kind, message, element);
    }

}
