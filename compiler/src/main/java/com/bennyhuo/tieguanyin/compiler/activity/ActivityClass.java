package com.bennyhuo.tieguanyin.compiler.activity;

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;
import com.bennyhuo.tieguanyin.annotations.SharedElement;
import com.bennyhuo.tieguanyin.annotations.SharedElementByNames;
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass;
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
import java.util.Collections;
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

public class ActivityClass {
    private static final String METHOD_NAME = "start";
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

    private ActivityResultClass activityResultClass;
    private GenerateMode generateMode;

    public final String simpleName;
    public final String packageName;

    private ActivityClass superActivityClass;

    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<String> categoriesRecursively = null;
    private ArrayList<Integer> flags = new ArrayList<>();
    private ArrayList<Integer> flagsRecursively = null;

    public ActivityClass(TypeElement type) {
        this.type = type;
        simpleName = TypeUtils.simpleName(type.asType());
        packageName = TypeUtils.getPackageName(type);

        Metadata metadata = type.getAnnotation(Metadata.class);
        //如果有这个注解，说明就是 Kotlin 类。
        boolean isKotlin = metadata != null;

        ActivityBuilder generateBuilder = type.getAnnotation(ActivityBuilder.class);
        if(generateBuilder.resultTypes().length > 0){
            activityResultClass = new ActivityResultClass(this, generateBuilder.resultTypes());
        }
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

        for (int flag : generateBuilder.flags()) {
            flags.add(flag);
        }

        Collections.addAll(categories, generateBuilder.categories());
    }

    public void setupSuperClass(HashMap<Element, ActivityClass> activityClasses){
        TypeMirror typeMirror = type.getSuperclass();
        if(typeMirror == null || typeMirror == Type.noType){
            return;
        }
        TypeElement superClassElement = (TypeElement) ((DeclaredType)typeMirror).asElement();
        this.superActivityClass = activityClasses.get(superClassElement);
        if(this.superActivityClass != null && this.activityResultClass != null){
            this.activityResultClass.setSuperActivityResultClass(this.superActivityClass.activityResultClass);
        }
    }

    public void addSymbol(RequiredField field) {
        if (field.isRequired()) {
            requiredFields.add(field);
        } else {
            optionalFields.add(field);
        }
    }

    public ArrayList<String> getCategoriesRecursively(){
        if(superActivityClass == null){
            return categories;
        }

        if(categoriesRecursively == null){
            categoriesRecursively = new ArrayList<>();
            categoriesRecursively.addAll(categories);
            categoriesRecursively.addAll(superActivityClass.getCategoriesRecursively());
        }
        return categoriesRecursively;
    }

    public ArrayList<Integer> getFlagsRecursively(){
        if(superActivityClass == null){
            return flags;
        }

        if(flagsRecursively == null){
            flagsRecursively = new ArrayList<>();
            flagsRecursively.addAll(flags);
            flagsRecursively.addAll(superActivityClass.getFlagsRecursively());
        }
        return flagsRecursively;
    }

    private Set<RequiredField> getRequiredFieldsRecursively() {
        if(superActivityClass == null){
            return requiredFields;
        }
        if(requiredFieldsRecursively == null){
            requiredFieldsRecursively = new TreeSet<>();
            requiredFieldsRecursively.addAll(requiredFields);
            requiredFieldsRecursively.addAll(superActivityClass.getRequiredFieldsRecursively());
        }
        return requiredFieldsRecursively;
    }

    public ArrayList<SharedElementEntity> getSharedElementsRecursively(){
        if(superActivityClass == null){
            return sharedElements;
        }
        if(sharedElementsRecursively == null){
            sharedElementsRecursively = new ArrayList<>(sharedElements);
            sharedElementsRecursively.addAll(superActivityClass.getSharedElementsRecursively());
        }
        return sharedElementsRecursively;
    }

    private Set<RequiredField> getOptionalFieldsRecursively() {
        if(superActivityClass == null){
            return optionalFields;
        }
        if(optionalFieldsRecursively == null){
            optionalFieldsRecursively = new TreeSet<>();
            optionalFieldsRecursively.addAll(optionalFields);
            optionalFieldsRecursively.addAll(superActivityClass.getOptionalFieldsRecursively());
        }
        return optionalFieldsRecursively;
    }

    public TypeElement getType() {
        return type;
    }

    private void buildConstants(TypeSpec.Builder typeBuilder){
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
        if(activityResultClass != null){
            for (ResultEntity resultEntity : activityResultClass.getResultEntitiesRecursively()) {
                typeBuilder.addField(FieldSpec.builder(String.class,
                        CONSTS_RESULT_PREFIX + Utils.camelToUnderline(resultEntity.name()),
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", resultEntity.name())
                        .build());
            }
        }
    }

    private void buildSaveStateMethod(TypeSpec.Builder typeBuilder){
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

    private void buildStartMethod(TypeSpec.Builder typeBuilder) {
        StartMethod startMethod = new StartMethod(this, METHOD_NAME);
        for (RequiredField field : getRequiredFieldsRecursively()) {
            startMethod.visitField(field);
        }

        StartMethod startMethodNoOptional = startMethod.copy(METHOD_NAME_NO_OPTIONAL);

        for (RequiredField field : getOptionalFieldsRecursively()) {
            startMethod.visitField(field);
        }
        startMethod.endWithResult(activityResultClass);
        typeBuilder.addMethod(startMethod.build());
        typeBuilder.addMethod(startMethod.buildForView());

        ArrayList<RequiredField> optionalBindings = new ArrayList<>(getOptionalFieldsRecursively());
        int size = optionalBindings.size();
        //选择长度为 i 的参数列表
        for (int step = 1; step < size; step++) {
            for (int start = 0; start < size; start++) {
                ArrayList<String> names = new ArrayList<>();
                StartMethod method = startMethodNoOptional.copy(METHOD_NAME_FOR_OPTIONAL);
                for(int index = start; index < step + start; index++){
                    RequiredField binding = optionalBindings.get(index % size);
                    method.visitField(binding);
                    names.add(Utils.capitalize(binding.getName()));
                }
                method.endWithResult(activityResultClass);
                method.renameTo(METHOD_NAME_FOR_OPTIONAL + Utils.joinString(names, METHOD_NAME_SEPARATOR));
                typeBuilder.addMethod(method.build());
                typeBuilder.addMethod(method.buildForView());
            }
        }

        if (size > 0) {
            startMethodNoOptional.endWithResult(activityResultClass);
            typeBuilder.addMethod(startMethodNoOptional.build());
            typeBuilder.addMethod(startMethodNoOptional.buildForView());
        }
    }

    private void buildInjectMethod(TypeSpec.Builder typeBuilder) {
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

    public void buildStartFunKt(FileSpec.Builder fileSpecBuilder) {
        StartFunctionKt startMethodKt = new StartFunctionKt(this, simpleName + POSIX, EXT_FUN_NAME_PREFIX + simpleName);

        for (RequiredField field : getRequiredFieldsRecursively()) {
            startMethodKt.visitField(field);
        }

        for (RequiredField field : getOptionalFieldsRecursively()) {
            startMethodKt.visitField(field);
        }

        startMethodKt.endWithResult(activityResultClass);
        fileSpecBuilder.addFunction(startMethodKt.buildForContext());
        fileSpecBuilder.addFunction(startMethodKt.buildForView());
        fileSpecBuilder.addFunction(startMethodKt.buildForFragment());
    }

    public void brew(Filer filer) {
        if(type.getModifiers().contains(Modifier.ABSTRACT)) return;
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        buildConstants(typeBuilder);

        buildInjectMethod(typeBuilder);

        buildSaveStateMethod(typeBuilder);

        if(activityResultClass != null){
            typeBuilder.addType(activityResultClass.buildOnActivityResultListenerInterface());
        }

        switch (generateMode) {
            case JavaOnly:
                buildStartMethod(typeBuilder);
                if(activityResultClass != null){
                    typeBuilder.addMethod(activityResultClass.buildFinishWithResultMethod());
                }
                break;
            case Both:
                buildStartMethod(typeBuilder);
                if(activityResultClass != null){
                    typeBuilder.addMethod(activityResultClass.buildFinishWithResultMethod());
                }
            case KotlinOnly:
                //region kotlin
                FileSpec.Builder fileSpecBuilder = FileSpec.builder(packageName, simpleName + POSIX);
                buildStartFunKt(fileSpecBuilder);
                if (activityResultClass != null) {
                    fileSpecBuilder.addFunction(activityResultClass.buildFinishWithResultKt());
                }
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
