package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.annotations.FragmentBuilder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.annotations.SharedElement;
import com.bennyhuo.tieguanyin.annotations.SharedElementByNames;
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity;
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FileSpec;
import com.sun.tools.javac.code.Type;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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

    private TreeSet<RequiredField> requiredFieldsRecursively = null;
    private TreeSet<RequiredField> optionalFieldsRecursively = null;

    private ArrayList<SharedElementEntity> sharedElements = new ArrayList<>();
    private ArrayList<SharedElementEntity> sharedElementsRecursively = null;

    private GenerateMode generateMode;

    public final String simpleName;
    public final String packageName;
    private FragmentClass superFragmentClass;

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

        for (SharedElement sharedElement : generateBuilder.sharedElements()) {
            sharedElements.add(new SharedElementEntity(sharedElement));
        }

        for (SharedElementByNames sharedElementByNames : generateBuilder.sharedElementsByNames()) {
            sharedElements.add(new SharedElementEntity(sharedElementByNames));
        }

        for (SharedElementWithName sharedElementWithName : generateBuilder.sharedElementsWithName()) {
            sharedElements.add(new SharedElementEntity(sharedElementWithName));
        }
    }

    public void setupSuperClass(HashMap<Element, FragmentClass> fragmentClasses) {
        TypeMirror typeMirror = type.getSuperclass();
        if (typeMirror == null || typeMirror == Type.noType) {
            return;
        }
        TypeElement superClassElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
        this.superFragmentClass = fragmentClasses.get(superClassElement);
    }

    public void addSymbol(RequiredField field) {
        if (field.isRequired()) {
            requiredFields.add(field);
        } else {
            optionalFields.add(field);
        }
    }

    public ArrayList<SharedElementEntity> getSharedElementsRecursively(){
        if(superFragmentClass == null){
            return sharedElements;
        }
        if(sharedElementsRecursively == null){
            sharedElementsRecursively = new ArrayList<>(sharedElements);
            sharedElementsRecursively.addAll(superFragmentClass.getSharedElementsRecursively());
        }
        return sharedElementsRecursively;
    }

    private Set<RequiredField> getRequiredFieldsRecursively() {
        if(superFragmentClass == null){
            return requiredFields;
        }
        if(requiredFieldsRecursively == null){
            requiredFieldsRecursively = new TreeSet<>();
            requiredFieldsRecursively.addAll(requiredFields);
            requiredFieldsRecursively.addAll(superFragmentClass.getRequiredFieldsRecursively());
        }
        return requiredFieldsRecursively;
    }

    private Set<RequiredField> getOptionalFieldsRecursively() {
        if(superFragmentClass == null){
            return optionalFields;
        }
        if(optionalFieldsRecursively == null){
            optionalFieldsRecursively = new TreeSet<>();
            optionalFieldsRecursively.addAll(optionalFields);
            optionalFieldsRecursively.addAll(superFragmentClass.getOptionalFieldsRecursively());
        }
        return optionalFieldsRecursively;
    }

    public TypeElement getType() {
        return type;
    }

    private void buildConstants(TypeSpec.Builder typeBuilder) {
        for (RequiredField field : getRequiredFieldsRecursively()) {
            typeBuilder.addField(FieldSpec.builder(String.class,
                    CONSTS_REQUIRED_FIELD_PREFIX + Utils.camelToUnderline(field.getName()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", field.getName())
                    .build());
        }
        for (RequiredField field : getOptionalFieldsRecursively()) {
            typeBuilder.addField(FieldSpec.builder(String.class,
                    CONSTS_OPTIONAL_FIELD_PREFIX + Utils.camelToUnderline(field.getName()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", field.getName())
                    .build());
        }
    }

    public void buildShowMethod(TypeSpec.Builder typeBuilder) {
        ShowMethod showMethod = new ShowMethod(this, METHOD_NAME);
        for (RequiredField field : getRequiredFieldsRecursively()) {
            showMethod.visitField(field);
        }

        ShowMethod showMethodNoOptional = showMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField field : getOptionalFieldsRecursively()) {
            showMethod.visitField(field);
        }
        showMethod.end();
        typeBuilder.addMethod(showMethod.build());

        ArrayList<RequiredField> optionalBindings = new ArrayList<>(getOptionalFieldsRecursively());
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

        for (RequiredField field : getRequiredFieldsRecursively()) {
            injectMethod.visitField(field);
        }

        for (RequiredField field : getOptionalFieldsRecursively()) {
            injectMethod.visitField(field);
        }
        injectMethod.end();

        typeBuilder.addMethod(injectMethod.build());
    }

    public void buildShowFunKt(FileSpec.Builder fileSpecBuilder) {
        ShowFunctionKt showMethodKt = new ShowFunctionKt(this, simpleName + POSIX, EXT_FUN_NAME_PREFIX + simpleName);

        for (RequiredField field : getRequiredFieldsRecursively()) {
            showMethodKt.visitField(field);
        }

        for (RequiredField field : getOptionalFieldsRecursively()) {
            showMethodKt.visitField(field);
        }

        showMethodKt.end();
        fileSpecBuilder.addFunction(showMethodKt.buildForContext());
        fileSpecBuilder.addFunction(showMethodKt.buildForView());
        fileSpecBuilder.addFunction(showMethodKt.buildForFragment());
    }

    public void buildSaveStateMethod(TypeSpec.Builder typeBuilder){
        SaveStateMethod saveStateMethod = new SaveStateMethod(this);
        for (RequiredField field : getRequiredFieldsRecursively()) {
            saveStateMethod.visitField(field);
        }

        for (RequiredField field : getOptionalFieldsRecursively()) {
            saveStateMethod.visitField(field);
        }
        saveStateMethod.end();
        typeBuilder.addMethod(saveStateMethod.build());
    }

    public void brew(Filer filer) {
        if(type.getModifiers().contains(Modifier.ABSTRACT)) return;

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        buildConstants(typeBuilder);

        buildInjectMethod(typeBuilder);

        buildSaveStateMethod(typeBuilder);

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
