package com.bennyhuo.compiler.fragment;

import com.bennyhuo.activitybuilder.annotations.FragmentBuilder;
import com.bennyhuo.activitybuilder.annotations.GenerateMode;
import com.bennyhuo.compiler.basic.RequiredField;
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

public class FragmentClass {
    private static final String METHOD_NAME = "show";
    private static final String METHOD_NAME_NO_OPTIONAL = METHOD_NAME + "WithoutOptional";
    private static final String METHOD_NAME_FOR_OPTIONAL = METHOD_NAME + "WithOptional";
    private static final String METHOD_NAME_SEPARATOR = "And";
    private static final String EXT_FUN_NAME_PREFIX = METHOD_NAME;
    private static final String POSIX = "Builder";

    private TypeElement type;
    private TreeSet<RequiredField> optionalFields = new TreeSet<>();
    private TreeSet<RequiredField> requiredFields = new TreeSet<>();
    private GenerateMode generateMode;

    public final String simpleName;
    public final String packageName;

    public FragmentClass(TypeElement type) {
        this.type = type;
        simpleName = TypeUtils.simpleName(type.asType());
        packageName = TypeUtils.getPackageName(type);

        Metadata metadata = type.getAnnotation(Metadata.class);
        //如果有这个注解，说明就是 Kotlin 类。
        boolean isKotlin = metadata != null;

        FragmentBuilder generateBuilder = type.getAnnotation(FragmentBuilder.class);
        generateMode = generateBuilder.mode();
        if(generateMode == GenerateMode.Auto){
            if(isKotlin) generateMode = GenerateMode.Both;
            else generateMode = GenerateMode.JavaOnly;
        }
    }

    public void addSymbol(RequiredField field) {
        if (field.isRequired()) {
            requiredFields.add(field);
        } else {
            optionalFields.add(field);
        }
    }

    public Set<RequiredField> getRequiredFields() {
        return requiredFields;
    }

    public Set<RequiredField> getOptionalFields() {
        return optionalFields;
    }

    public TypeElement getType() {
        return type;
    }

    public void buildOpenMethod(TypeSpec.Builder typeBuilder) {
        OpenMethod openMethod = new OpenMethod(this, METHOD_NAME);
        for (RequiredField field : getRequiredFields()) {
            openMethod.visitField(field);
        }

        OpenMethod openMethodNoOptional = openMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField field : getOptionalFields()) {
            openMethod.visitField(field);
        }
        openMethod.end();
        typeBuilder.addMethod(openMethod.build());

        ArrayList<RequiredField> optionalBindings = new ArrayList<>(getOptionalFields());
        int size = optionalBindings.size();
        //选择长度为 i 的参数列表
        for (int step = 1; step < size; step++) {
            for (int start = 0; start < size; start++) {
                ArrayList<String> names = new ArrayList<>();
                OpenMethod method = openMethodNoOptional.copy(METHOD_NAME_FOR_OPTIONAL);
                for(int index = start; index < step + start; index++){
                    RequiredField binding = optionalBindings.get(index % size);
                    method.visitField(binding);
                    names.add(Utils.capitalize(binding.getName()));
                }
                method.end();
                method.renameTo(METHOD_NAME_FOR_OPTIONAL + Utils.joinString(names, METHOD_NAME_SEPARATOR));
                typeBuilder.addMethod(method.build());
            }
        }

        if (size > 0) {
            openMethodNoOptional.end();
            typeBuilder.addMethod(openMethodNoOptional.build());
        }
    }

    public void buildInjectMethod(TypeSpec.Builder typeBuilder) {
        InjectMethod injectMethod = new InjectMethod(this);

        for (RequiredField field : getRequiredFields()) {
            injectMethod.visitField(field);
        }

        for (RequiredField field : getOptionalFields()) {
            injectMethod.visitField(field);
        }
        injectMethod.end();

        typeBuilder.addMethod(injectMethod.build());
    }

    public void buildOpenFunKt(FileSpec.Builder fileSpecBuilder) {
        OpenMethodKt openMethodKt = new OpenMethodKt(this, simpleName + POSIX, EXT_FUN_NAME_PREFIX + simpleName);

        for (RequiredField field : getRequiredFields()) {
            openMethodKt.visitField(field);
        }

        for (RequiredField field : getOptionalFields()) {
            openMethodKt.visitField(field);
        }

        openMethodKt.end();
        fileSpecBuilder.addFunction(openMethodKt.buildForContext());
        fileSpecBuilder.addFunction(openMethodKt.buildForView());
        fileSpecBuilder.addFunction(openMethodKt.buildForFragment());
    }

    public void brew(Filer filer) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        buildInjectMethod(typeBuilder);

        switch (generateMode) {
            case JavaOnly:
                buildOpenMethod(typeBuilder);
                break;
            case Both:
                buildOpenMethod(typeBuilder);
            case KotlinOnly:
                //region kotlin
                FileSpec.Builder fileSpecBuilder = FileSpec.builder(packageName, simpleName + POSIX);
                buildOpenFunKt(fileSpecBuilder);
                writeKotlinToFile(filer, fileSpecBuilder.build());
                //endregion
                break;
        }

        writeJavaToFile(filer, typeBuilder.build());
    }

    private void writeJavaToFile(Filer filer, TypeSpec typeSpec){
        try {
            JavaFile file = JavaFile.builder(packageName, typeSpec).build();
            file.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeKotlinToFile(Filer filer, FileSpec fileSpec){
        try {
            FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileSpec.getName() + ".kt");
            Writer writer = fileObject.openWriter();
            fileSpec.writeTo(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
