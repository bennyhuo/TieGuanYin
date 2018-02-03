package com.bennyhuo.compiler.basic;

import com.bennyhuo.activitybuilder.runtime.annotations.GenerateBuilder;
import com.bennyhuo.compiler.result.ActivityResultClass;
import com.bennyhuo.compiler.utils.TypeUtils;
import com.bennyhuo.compiler.utils.Utils;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FileSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import kotlin.Metadata;

/**
 * Created by benny on 1/29/18.
 */

public class ActivityClass {
    private static final String METHOD_NAME = "open";
    private static final String METHOD_NAME_NO_OPTIONAL = "openWithoutOptional";
    private static final String METHOD_NAME_FOR_OPTIONAL = "openWithOptional";
    private static final String METHOD_NAME_SEPARATOR = "And";
    private static final String EXT_FUN_NAME_PREFIX = "open";
    private static final String POSIX = "Builder";

    private TypeElement type;
    private TreeSet<RequiredField> optionalBindings = new TreeSet<>();
    private TreeSet<RequiredField> requiredBindings = new TreeSet<>();
    private ActivityResultClass activityResultClass;
    private boolean isKotlin;
    private boolean forceJava;

    public final String simpleName;
    public final String packageName;

    public ActivityClass(TypeElement type) {
        this.type = type;
        simpleName = TypeUtils.simpleName(type.asType());
        packageName = TypeUtils.getPackageName(type);

        Metadata metadata = type.getAnnotation(Metadata.class);
        //如果有这个注解，说明就是 Kotlin 类。
        isKotlin = metadata != null;

        GenerateBuilder generateBuilder = type.getAnnotation(GenerateBuilder.class);
        if(generateBuilder.forResult()){
            activityResultClass = new ActivityResultClass(this, generateBuilder.resultTypes());
        }
        forceJava = generateBuilder.forceJava();
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

    public boolean isForceJava() {
        return forceJava;
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

    public void brewJava(Filer filer){
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        KotlinOpenMethod kotlinOpenMethod = new KotlinOpenMethod(this, simpleName + POSIX, EXT_FUN_NAME_PREFIX + simpleName);
        OpenMethod openMethod = new OpenMethod(this, METHOD_NAME);
        InjectMethod injectMethod = new InjectMethod(this);

        for (RequiredField binding : getRequiredBindings()) {
            openMethod.visitBinding(binding);
            kotlinOpenMethod.visitBinding(binding);
            injectMethod.visitBinding(binding);
        }

        OpenMethod openMethodNoOptional = openMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField optionalBinding : getOptionalBindings()) {
            openMethod.visitBinding(optionalBinding);
            kotlinOpenMethod.visitBinding(optionalBinding);
            injectMethod.visitBinding(optionalBinding);
        }

        openMethod.endWithResult(activityResultClass);
        kotlinOpenMethod.endWithResult(activityResultClass);
        injectMethod.end();

        typeBuilder.addMethod(injectMethod.build())
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
                method.renameTo(METHOD_NAME_FOR_OPTIONAL + Utils.joinString(names, METHOD_NAME_SEPARATOR));
                typeBuilder.addMethod(method.build());
            }
        }

        if (size > 0) {
            openMethodNoOptional.endWithResult(activityResultClass);
            typeBuilder.addMethod(openMethodNoOptional.build());
        }

        if (isKotlin) {
            FileSpec.Builder fileSpecBuilder = FileSpec.builder(packageName, simpleName + "Ext");
            fileSpecBuilder.addFunction(kotlinOpenMethod.build());
            if(activityResultClass != null){
                typeBuilder.addType(activityResultClass.buildListenerInterface());
                fileSpecBuilder.addFunction(activityResultClass.buildFinishWithResultExt());
            }
            try {
                FileSpec fileSpec = fileSpecBuilder.build();
                FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileSpec.getName() + ".kt");
                Writer writer = fileObject.openWriter();
                fileSpec.writeTo(writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(activityResultClass != null){
                typeBuilder.addType(activityResultClass.buildListenerInterface());
                typeBuilder.addMethod(activityResultClass.buildFinishWithResultMethod());
            }
        }

        try {
            JavaFile file = JavaFile.builder(packageName, typeBuilder.build()).build();
            file.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
