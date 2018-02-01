package com.bennyhuo.compiler.result;

import com.bennyhuo.activitybuilder.OnActivityResultListener;
import com.bennyhuo.annotations.ResultEntity;
import com.bennyhuo.compiler.utils.JavaTypes;
import com.bennyhuo.compiler.utils.KotlinTypes;
import com.bennyhuo.compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.TypeNames;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by benny on 1/31/18.
 */
public class ActivityResultClass {

    private ResultEntity[] resultEntities;

    private TypeElement activityType;

    private MethodSpec.Builder onResultMethodBuilder;
    private MethodSpec.Builder interfaceOnResultMethodBuilder;
    private MethodSpec.Builder finishWithResultMethodBuilder;

    private String packageName;

    public ActivityResultClass(String packageName, TypeElement activityType, ResultEntity[] resultEntities) {
        this.packageName = packageName;
        this.activityType = activityType;
        this.resultEntities = resultEntities;

        interfaceOnResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(TypeName.VOID);

        finishWithResultMethodBuilder = MethodSpec.methodBuilder("finishWithResult")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(ClassName.get(activityType), "activity")
                .returns(TypeName.VOID)
                .addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);

        onResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JavaTypes.BUNDLE, "bundle")
                .returns(TypeName.VOID);

        onResultMethodBuilder.beginControlFlow("if($L != null)", "on" + activityType.getSimpleName().toString() + "ResultListener");
        StringBuilder statementBuilder = new StringBuilder();
        ArrayList<Object> args = new ArrayList<>();

        args.add("on" + activityType.getSimpleName().toString() + "ResultListener");
        for (ResultEntity resultEntity : resultEntities) {
            TypeMirror typeMirror = null;
            try {
                resultEntity.type();
            } catch (MirroredTypeException e) {
                typeMirror = e.getTypeMirror();
            }
            TypeName resultClass = ClassName.get(typeMirror);
            interfaceOnResultMethodBuilder.addParameter(resultClass, resultEntity.name());
            statementBuilder.append("$T.<$T>get(bundle, $S),");
            args.add(JavaTypes.RUNTIME_UTILS);
            args.add(resultClass.box());
            args.add(resultEntity.name());

            finishWithResultMethodBuilder.addParameter(ClassName.get(typeMirror), resultEntity.name());
            finishWithResultMethodBuilder.addStatement("intent.putExtra($S, $L)", resultEntity.name(), resultEntity.name());
        }
        //onHelloInJavaActivityResultListener.onResult(Utils.<Integer>get(bundle, "kotlin"), Utils.<String>get(bundle, "java"));
        statementBuilder.deleteCharAt(statementBuilder.length() - 1);
        onResultMethodBuilder.addStatement("$L.onResult(" + statementBuilder.toString() + ")", args.toArray());
        onResultMethodBuilder.endControlFlow();
    }

    public TypeSpec buildListenerInterface() {
        return TypeSpec.interfaceBuilder(ClassName.get(packageName, "On" + activityType.getSimpleName().toString() + "ResultListener"))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(interfaceOnResultMethodBuilder.build())
                .build();
    }

    public ClassName getListenerClass(){
        return ClassName.get(packageName + "." +activityType.getSimpleName().toString() + "Builder", "On" + activityType.getSimpleName().toString() + "ResultListener");
    }

    public String getListenerName(){
        return "on" + activityType.getSimpleName().toString() + "ResultListener";
    }

    public TypeSpec createOnResultListenerObject() {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(OnActivityResultListener.class)
                .addMethod(onResultMethodBuilder.build())
                .build();
    }

    public FileSpec createKotlinExt() {
        FunSpec.Builder funBuilder = FunSpec.builder("finishWithResult")
                .receiver(TypeNames.get(activityType.asType()));

        funBuilder.addStatement("val intent = %T()", KotlinTypes.INTENT);
        for (ResultEntity resultEntity : resultEntities) {
            TypeMirror typeMirror = null;
            try {
                resultEntity.type();
            } catch (MirroredTypeException e) {
                typeMirror = e.getTypeMirror();
            }
            funBuilder.addParameter(resultEntity.name(), Utils.toKotlinType(typeMirror));
            funBuilder.addStatement("intent.putExtra(%S, %L)", resultEntity.name(), resultEntity.name());
        }
        funBuilder.addStatement("setResult(1, intent)");
        funBuilder.addStatement("finish()");
        return FileSpec.builder(packageName, activityType.getSimpleName().toString() + "Ext")
                .addFunction(funBuilder.build()).build();
    }

    public MethodSpec buildFinishWithResultMethod() {
        return finishWithResultMethodBuilder.addStatement("activity.setResult(1, intent)").addStatement("activity.finish()").build();
    }
}
