package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.annotations.FragmentBuilder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.FieldSpec;
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

    private static final String CONSTS_REQUIRED_FIELD_PREFIX = "REQUIRED_";
    private static final String CONSTS_OPTIONAL_FIELD_PREFIX = "OPTIONAL_";
    private static final String CONSTS_RESULT_PREFIX = "RESULT_";

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

    private void buildConstants(TypeSpec.Builder typeBuilder) {
        for (RequiredField field : requiredFields) {
            typeBuilder.addField(FieldSpec.builder(String.class,
                    CONSTS_REQUIRED_FIELD_PREFIX + Utils.camelToUnderline(field.getName()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", field.getName())
                    .build());
        }
        for (RequiredField field : optionalFields) {
            typeBuilder.addField(FieldSpec.builder(String.class,
                    CONSTS_OPTIONAL_FIELD_PREFIX + Utils.camelToUnderline(field.getName()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", field.getName())
                    .build());
        }
    }

    public void buildShowMethod(TypeSpec.Builder typeBuilder) {
        ShowMethod showMethod = new ShowMethod(this, METHOD_NAME);
        for (RequiredField field : getRequiredFields()) {
            showMethod.visitField(field);
        }

        ShowMethod showMethodNoOptional = showMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField field : getOptionalFields()) {
            showMethod.visitField(field);
        }
        showMethod.end();
        typeBuilder.addMethod(showMethod.build());

        ArrayList<RequiredField> optionalBindings = new ArrayList<>(getOptionalFields());
        int size = optionalBindings.size();
        //选择长度为 i 的参数列表
        for (int step = 1; step < size; step++) {
            for (int start = 0; start < size; start++) {
                ArrayList<String> names = new ArrayList<>();
                ShowMethod method = showMethodNoOptional.copy(METHOD_NAME_FOR_OPTIONAL);
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
            showMethodNoOptional.end();
            typeBuilder.addMethod(showMethodNoOptional.build());
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

    public void buildShowFunKt(FileSpec.Builder fileSpecBuilder) {
        ShowFunctionKt showMethodKt = new ShowFunctionKt(this, simpleName + POSIX, EXT_FUN_NAME_PREFIX + simpleName);

        for (RequiredField field : getRequiredFields()) {
            showMethodKt.visitField(field);
        }

        for (RequiredField field : getOptionalFields()) {
            showMethodKt.visitField(field);
        }

        showMethodKt.end();
        fileSpecBuilder.addFunction(showMethodKt.buildForContext());
        fileSpecBuilder.addFunction(showMethodKt.buildForView());
        fileSpecBuilder.addFunction(showMethodKt.buildForFragment());
    }

    public void brew(Filer filer) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        buildConstants(typeBuilder);

        buildInjectMethod(typeBuilder);

        switch (generateMode) {
            case JavaOnly:
                buildShowMethod(typeBuilder);
                break;
            case Both:
                buildShowMethod(typeBuilder);
            case KotlinOnly:
                //region kotlin
                FileSpec.Builder fileSpecBuilder = FileSpec.builder(packageName, simpleName + POSIX);
                buildShowFunKt(fileSpecBuilder);
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
