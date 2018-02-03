package com.bennyhuo.compiler;

import com.bennyhuo.activitybuilder.runtime.annotations.GenerateBuilder;
import com.bennyhuo.activitybuilder.runtime.annotations.Optional;
import com.bennyhuo.activitybuilder.runtime.annotations.Required;
import com.bennyhuo.compiler.basic.ActivityClass;
import com.bennyhuo.compiler.basic.OptionalField;
import com.bennyhuo.compiler.basic.RequiredField;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by benny on 10/2/16.
 */
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    public static final String TAG = "BuilderProcessor";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        processingEnvironment = env;
        Logger.messager = env.getMessager();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(GenerateBuilder.class);
        annotations.add(Required.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        HashMap<Element, ActivityClass> activityClasses = new HashMap<>();
        parse(env, activityClasses);

        for (ActivityClass activityClass : activityClasses.values()) {
            activityClass.brewJava(filer);
        }
        return true;
    }

    private void parse(RoundEnvironment env, HashMap<Element, ActivityClass> activityClasses) {
        for (Element element : env.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind().isClass()) {
                    activityClasses.put(element, new ActivityClass(processingEnv, (TypeElement) element));
                }
            } catch (Exception e) {
                logParsingError(element, GenerateBuilder.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(Required.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    ActivityClass activityClass = activityClasses.get(element.getEnclosingElement());
                    if (activityClass == null) {
                        error(element, "Field " + element + " annotated as Required while " + element.getEnclosingElement() + " not annotated.");
                    } else {
                        activityClass.addSymbol(new RequiredField((Symbol.VarSymbol) element));
                    }
                }
            } catch (Exception e) {
                logParsingError(element, Required.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(Optional.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    ActivityClass activityClass = activityClasses.get(element.getEnclosingElement());
                    if (activityClass == null) {
                        error(element, "Field " + element + " annotated as Optional while " + element.getEnclosingElement() + " not annotated.");
                    } else {
                        activityClass.addSymbol(new OptionalField((Symbol.VarSymbol) element));
                    }
                }
            } catch (Exception e) {
                logParsingError(element, Required.class, e);
            }
        }
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
}
