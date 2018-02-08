package com.bennyhuo.tieguanyin.compiler;

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder;
import com.bennyhuo.tieguanyin.annotations.FragmentBuilder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass;
import com.bennyhuo.tieguanyin.compiler.basic.OptionalField;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass;
import com.bennyhuo.tieguanyin.compiler.utils.Logger;
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;

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

/**
 * Created by benny on 10/2/16.
 */
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    public static final String TAG = "BuilderProcessor";

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();

        TypeUtils.types = env.getTypeUtils();
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
        annotations.add(ActivityBuilder.class);
        annotations.add(FragmentBuilder.class);
        annotations.add(Required.class);
        annotations.add(Optional.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        HashMap<Element, ActivityClass> activityClasses = new HashMap<>();
        HashMap<Element, FragmentClass> fragmentClasses = new HashMap<>();
        parseActivityClass(env, activityClasses);
        parseFragmentClass(env, fragmentClasses);
        parseFields(env, activityClasses, fragmentClasses);
        brewFiles(activityClasses, fragmentClasses);
        return true;
    }

    private void brewFiles(HashMap<Element, ActivityClass> activityClasses, HashMap<Element, FragmentClass> fragmentClasses){
        for (ActivityClass activityClass : activityClasses.values()) {
            activityClass.brew(filer);
        }

        for (FragmentClass fragmentClass : fragmentClasses.values()) {
            fragmentClass.brew(filer);
        }
    }

    private void parseFields(RoundEnvironment env, HashMap<Element, ActivityClass> activityClasses, HashMap<Element, FragmentClass> fragmentClasses) {
        for (Element element : env.getElementsAnnotatedWith(Required.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    ActivityClass activityClass = activityClasses.get(element.getEnclosingElement());
                    if (activityClass == null) {
                        FragmentClass fragmentClass = fragmentClasses.get(element.getEnclosingElement());
                        if (fragmentClass == null) {
                            Logger.error(element, "Field " + element + " annotated as Required while " + element.getEnclosingElement() + " not annotated.");
                        } else {
                            fragmentClass.addSymbol(new RequiredField((Symbol.VarSymbol) element));
                        }
                    } else {
                        activityClass.addSymbol(new RequiredField((Symbol.VarSymbol) element));
                    }
                }
            } catch (Exception e) {
                Logger.logParsingError(element, Required.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(Optional.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    ActivityClass activityClass = activityClasses.get(element.getEnclosingElement());
                    if (activityClass == null) {
                        FragmentClass fragmentClass = fragmentClasses.get(element.getEnclosingElement());
                        if (fragmentClass == null) {
                            Logger.error(element, "Field " + element + " annotated as Optional while " + element.getEnclosingElement() + " not annotated.");
                        } else {
                            fragmentClass.addSymbol(new OptionalField((Symbol.VarSymbol) element));
                        }
                    } else {
                        activityClass.addSymbol(new OptionalField((Symbol.VarSymbol) element));
                    }
                }
            } catch (Exception e) {
                Logger.logParsingError(element, Required.class, e);
            }
        }
    }

    private void parseActivityClass(RoundEnvironment env, HashMap<Element, ActivityClass> activityClasses) {
        for (Element element : env.getElementsAnnotatedWith(ActivityBuilder.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind().isClass()) {
                    activityClasses.put(element, new ActivityClass((TypeElement) element));
                }
            } catch (Exception e) {
                Logger.logParsingError(element, ActivityBuilder.class, e);
            }
        }
    }

    private void parseFragmentClass(RoundEnvironment env, HashMap<Element, FragmentClass> fragmentClasses) {
        for (Element element : env.getElementsAnnotatedWith(FragmentBuilder.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind().isClass()) {
                    fragmentClasses.put(element, new FragmentClass((TypeElement) element));
                }
            } catch (Exception e) {
                Logger.logParsingError(element, FragmentBuilder.class, e);
            }
        }
    }


}
