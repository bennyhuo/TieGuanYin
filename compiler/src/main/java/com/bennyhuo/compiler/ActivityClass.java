package com.bennyhuo.compiler;

import com.bennyhuo.annotations.GenerateBuilder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FileSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by benny on 1/29/18.
 */

public class ActivityClass {
    private static final String METHOD_NAME = "open";
    private static final String METHOD_NAME_NO_OPTIONAL = "openWithoutOptional";
    private static final String METHOD_NAME_FOR_OPTIONAL = "openWithOptional";
    private static final String METHOD_NAME_SEPERATOR = "And";
    private static final String POSIX = "Builder";

    private ProcessingEnvironment env;
    private TypeElement type;
    private TreeSet<RequiredField> optionalBindings = new TreeSet<>();
    private TreeSet<RequiredField> requiredBindings = new TreeSet<>();
    private ActivityResultClass activityResultClass;
    private boolean isKotlin;

    public ActivityClass(ProcessingEnvironment env, TypeElement type, boolean isKotlin) {
        this.env = env;
        this.type = type;
        this.isKotlin = isKotlin;

        GenerateBuilder generateBuilder = type.getAnnotation(GenerateBuilder.class);
        if(generateBuilder.forResult()){
            activityResultClass = new ActivityResultClass(getPackage(), type, generateBuilder.resultTypes());
        }
    }

    public void addSymbol(RequiredField binding) {
        if (binding.isRequired()) {
            requiredBindings.add(binding);
        } else {
            optionalBindings.add(binding);
        }
    }

    public boolean isKotlin() {
        return isKotlin;
    }

    public Set<RequiredField> getRequiredBindings(){
        return requiredBindings;
    }

    public Set<RequiredField> getOptionalBindings() {
        return optionalBindings;
    }

    public TypeElement getType() {
        return type;
    }

    public String getPackage(){
        if(type.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)){
            return type.getEnclosingElement().asType().toString();
        }else{
            throw new IllegalArgumentException(type.getEnclosingElement().toString());
        }
    }


    public void brewJava(Filer filer){
        OpenMethod openMethod = new OpenMethod(this, METHOD_NAME);
        InjectMethod injectMethod = new InjectMethod(this);

        for (RequiredField binding : getRequiredBindings()) {
            openMethod.visitBinding(binding);
            injectMethod.visitBinding(binding);
        }

        OpenMethod openMethodNoOptional = openMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField optionalBinding : getOptionalBindings()) {
            openMethod.visitBinding(optionalBinding);
            injectMethod.visitBinding(optionalBinding);
        }

        openMethod.endWithResult(activityResultClass);
        injectMethod.end();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName(getType().asType()) + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(injectMethod.build())
                .addMethod(openMethod.build());

        ArrayList<RequiredField> optionalBindings = new ArrayList<>(getOptionalBindings());
        int size = optionalBindings.size();
        //选择长度为 i 的参数列表
        for (int step = 1; step < size; step++) {
            for (int start = 0; start < size; start++) {
                ArrayList<String> names = new ArrayList<>();
                OpenMethod method = openMethodNoOptional.copy(METHOD_NAME_FOR_OPTIONAL);
                for(int index = start; index < step + start; index++){
                    RequiredField binding = optionalBindings.get(index % size);
                    method.visitBinding(binding);
                    names.add(Utils.capitalize(binding.getName()));
                }
                method.endWithResult(activityResultClass);
                method.renameTo(METHOD_NAME_FOR_OPTIONAL + Utils.joinString(names, METHOD_NAME_SEPERATOR));
                typeBuilder.addMethod(method.build());
            }
        }

        if (size > 0) {
            openMethodNoOptional.endWithResult(activityResultClass);
            typeBuilder.addMethod(openMethodNoOptional.build());
        }

        if(activityResultClass != null){
            typeBuilder.addType(activityResultClass.buildListenerInterface());
            typeBuilder.addMethod(activityResultClass.buildFinishWithResultMethod());
        }

        try {
            JavaFile file = JavaFile.builder(getPackage(), typeBuilder.build()).build();
            file.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (activityResultClass != null && isKotlin) {
            try {
                FileSpec fileSpec = activityResultClass.createKotlinExt();
                FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, getPackage(), fileSpec.getName() + ".kt");
                Writer writer = fileObject.openWriter();
                fileSpec.writeTo(writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Uses both {@link Types#erasure} and string manipulation to strip any generic types.
     */
    private String doubleErasure(TypeMirror elementType) {
        String name = env.getTypeUtils().erasure(elementType).toString();
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
