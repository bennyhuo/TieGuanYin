package com.bennyhuo.compiler;

import com.bennyhuo.activitybuilder.ActivityBuilder;
import com.bennyhuo.activitybuilder.OnActivityCreateListener;
import com.bennyhuo.annotations.GenerateBuilder;
import com.bennyhuo.annotations.Optional;
import com.bennyhuo.annotations.Required;
import com.bennyhuo.utils.BundleUtils;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by benny on 10/2/16.
 */
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    public static final String TAG = "BuilderProcessor";

    public static final String POSIX = "Builder";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
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
        for (Element element : env.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                note(element, element.toString());
                if (element.getKind().isClass()) {
                    activityClasses.put(element, new ActivityClass((TypeElement) element));
                }
            } catch (Exception e) {
                logParsingError(element, GenerateBuilder.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(Required.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    activityClasses.get(element.getEnclosingElement()).addSymbol(new ParamBinding((Symbol.VarSymbol) element, true));
                }
            } catch (Exception e) {
                logParsingError(element, Required.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(Optional.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                if (element.getKind() == ElementKind.FIELD) {
                    activityClasses.get(element.getEnclosingElement()).addSymbol(new ParamBinding((Symbol.VarSymbol) element, false));
                }
            } catch (Exception e) {
                logParsingError(element, Required.class, e);
            }
        }

        for (ActivityClass activityClass : activityClasses.values()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("open")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeName.VOID)
                    .addParameter(ClassName.get("android.content", "Context"), "context")
                    .addStatement("$T.INSTANCE.init(context)", ActivityBuilder.class);

            MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeName.VOID);

            MethodSpec.Builder onActivityCreatedMethodBuilder = MethodSpec.methodBuilder("onActivityCreated")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("android.app", "Activity"), "activity")
                    .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                    .returns(TypeName.VOID)
                    .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
                    .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType())
                    .addStatement("$T extras = activity.getIntent().getExtras()", ClassName.get("android.os", "Bundle"));


            ClassName intentClass = ClassName.get("android.content", "Intent");
            methodBuilder.addStatement("$T intent = new $T(context, $T.class)", intentClass, intentClass, activityClass.getType());

            for (ParamBinding binding : activityClass.getRequiredBindings()) {
                note(binding.getSymbol(), "VarType：" + binding.getSymbol().type);
                String name = binding.getName();
                methodBuilder.addParameter(ClassName.get(binding.getSymbol().type), name);
                methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);

                Set<Modifier> modifiers = binding.getSymbol().getModifiers();
                Type type = binding.getSymbol().type;
                TypeName typeName;
                note(binding.getSymbol(), type.getClass().toString());
                if (type.isPrimitive()) {
                    typeName = Utils.toWrapperType(type);
                } else {
                    typeName = TypeName.get(type);
                }
                if (modifiers.contains(Modifier.PRIVATE)) {
                    onActivityCreatedMethodBuilder.addStatement("typedActivity.set$L($T.<$T>get(extras, $S))", Utils.capitalize(name), BundleUtils.class, typeName, name);
                } else {
                    onActivityCreatedMethodBuilder.addStatement("typedActivity.$L = $T.<$T>get(extras, $S)", name, BundleUtils.class, typeName, name);
                }
            }

            MethodSpec overloadBaseMethod = methodBuilder.build();

            for (ParamBinding optionalBinding : activityClass.optionalBindings) {
                String name = optionalBinding.getName();
                methodBuilder.addParameter(ClassName.get(optionalBinding.getSymbol().type), name);
                methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);

                Set<Modifier> modifiers = optionalBinding.getSymbol().getModifiers();
                Type type = optionalBinding.getSymbol().type;
                TypeName typeName;
                note(optionalBinding.getSymbol(), type.getClass().toString());
                if (type.isPrimitive()) {
                    typeName = Utils.toWrapperType(type);
                } else {
                    typeName = TypeName.get(type);
                }
                if (modifiers.contains(Modifier.PRIVATE)) {
                    onActivityCreatedMethodBuilder.addStatement("typedActivity.set$L($T.<$T>get(extras, $S))", Utils.capitalize(name), BundleUtils.class, typeName, name);
                } else {
                    onActivityCreatedMethodBuilder.addStatement("typedActivity.$L = $T.<$T>get(extras, $S)", name, BundleUtils.class, typeName, name);
                }
            }

            methodBuilder.addStatement("context.startActivity(intent)")
                    .addStatement("inject()");


            onActivityCreatedMethodBuilder.addStatement("$T.INSTANCE.removeOnActivityCreateListener(this)", ActivityBuilder.class)
                    .endControlFlow();
            TypeSpec onActivityCreateListenerType = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(OnActivityCreateListener.class)
                    .addMethod(onActivityCreatedMethodBuilder.build())
                    .build();
            injectMethodBuilder.addStatement("$T.INSTANCE.addOnActivityCreateListener($L)", ActivityBuilder.class, onActivityCreateListenerType);


            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName(activityClass.getType().asType()) + POSIX)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(injectMethodBuilder.build())
                    .addMethod(methodBuilder.build());

            ArrayList<ParamBinding> optionalBindings = new ArrayList<>(activityClass.getOptionalBindings());
            int size = optionalBindings.size();
            //选择长度为 i 的参数列表
            for (int step = 1; step < size; step++) {
                for (int start = 0; start < size; start++) {
                    ArrayList<String> names = new ArrayList<>();
                    MethodSpec.Builder builder = overloadBaseMethod.toBuilder();
                    for(int index = start; index < step + start; index++){
                        ParamBinding binding = optionalBindings.get(index % size);
                        String name = binding.getName();
                        builder.addParameter(ClassName.get(binding.getSymbol().type), name);
                        builder.addStatement("intent.putExtra($S, $L)", name, name);
                        names.add(Utils.capitalize(name));
                    }
                    builder.addStatement("context.startActivity(intent)").addStatement("inject()");
                    typeBuilder.addMethod(Utils.copyMethodWithNewName("openWithOptional" + Utils.joinString(names, "And"), builder.build()).build());
                }
            }

            if (size > 0) {
                typeBuilder.addMethod(overloadBaseMethod.toBuilder().addStatement("context.startActivity(intent)").addStatement("inject()").build());
            }

            JavaFile file = JavaFile.builder(activityClass.getPackage(), typeBuilder.build()).build();
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
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

    /**
     * Uses both {@link Types#erasure} and string manipulation to strip any generic types.
     */
    private String doubleErasure(TypeMirror elementType) {
        String name = typeUtils.erasure(elementType).toString();
        int typeParamStart = name.indexOf('<');
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart);
        }
        return name;
    }

    private String simpleName(TypeMirror elementType) {
        String name = doubleErasure(elementType);
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
