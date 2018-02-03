package com.bennyhuo.compiler.result;

import com.bennyhuo.activitybuilder.runtime.annotations.ResultEntity;
import com.bennyhuo.activitybuilder.runtime.core.OnActivityResultListener;
import com.bennyhuo.compiler.basic.ActivityClass;
import com.bennyhuo.compiler.utils.JavaTypes;
import com.bennyhuo.compiler.utils.KotlinTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.LambdaTypeName;
import com.squareup.kotlinpoet.TypeNames;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */
public class ActivityResultClass {

    private ResultEntity[] resultEntities;

    private FunSpec.Builder onResultFunBuilderKt;
    private MethodSpec.Builder onResultMethodBuilder;
    private MethodSpec.Builder interfaceOnResultMethodBuilder;
    private MethodSpec.Builder finishWithResultMethodBuilder;

    private LambdaTypeName onResultLambdaType;

    private ActivityClass activityClass;

    public ActivityResultClass(ActivityClass activityClass, ResultEntity[] resultEntities) {
        this.activityClass = activityClass;
        this.resultEntities = resultEntities;

        interfaceOnResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(TypeName.VOID);

        finishWithResultMethodBuilder = MethodSpec.methodBuilder("finishWithResult")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(ClassName.get(activityClass.getType()), "activity")
                .returns(TypeName.VOID)
                .addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);

        onResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JavaTypes.BUNDLE, "bundle")
                .returns(TypeName.VOID);

        onResultFunBuilderKt = FunSpec.builder("onResult")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", KotlinTypes.BUNDLE)
                .returns(TypeNames.UNIT);

        onResultMethodBuilder.beginControlFlow("if($L != null)", "on" + activityClass.simpleName + "ResultListener");
        onResultFunBuilderKt.beginControlFlow("if(%L != null)", "on" + activityClass.simpleName + "ResultListener");
        StringBuilder statementBuilder = new StringBuilder();
        StringBuilder statementBuilderKt = new StringBuilder();
        ArrayList<Object> args = new ArrayList<>();
        ArrayList<Object> argsKt = new ArrayList<>();
        ArrayList<com.squareup.kotlinpoet.TypeName> argTypeNames = new ArrayList<>();

        args.add("on" + activityClass.simpleName + "ResultListener");
        argsKt.add("on" + activityClass.simpleName + "ResultListener");
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
            statementBuilderKt.append("%T.get(bundle, %S),");
            args.add(JavaTypes.RUNTIME_UTILS);
            args.add(resultClass.box());
            args.add(resultEntity.name());

            argsKt.add(KotlinTypes.RUNTIME_UTILS);
            argsKt.add(resultEntity.name());

            argTypeNames.add(KotlinTypes.toKotlinType(typeMirror));

            finishWithResultMethodBuilder.addParameter(ClassName.get(typeMirror), resultEntity.name());
            finishWithResultMethodBuilder.addStatement("intent.putExtra($S, $L)", resultEntity.name(), resultEntity.name());
        }
        //onHelloInJavaActivityResultListener.onResult(Utils.<Integer>get(bundle, "kotlin"), Utils.<String>get(bundle, "java"));
        statementBuilder.deleteCharAt(statementBuilder.length() - 1);
        statementBuilderKt.deleteCharAt(statementBuilderKt.length() - 1);
        onResultMethodBuilder.addStatement("$L.onResult(" + statementBuilder.toString() + ")", args.toArray());
        onResultMethodBuilder.endControlFlow();
        onResultFunBuilderKt.addStatement("%L(" + statementBuilderKt.toString() + ")", argsKt.toArray());
        onResultFunBuilderKt.endControlFlow();

        onResultLambdaType = LambdaTypeName.get(null, argTypeNames.toArray(new com.squareup.kotlinpoet.TypeName[0]), TypeNames.get(Unit.class));
    }

    public TypeSpec buildListenerInterface() {
        return TypeSpec.interfaceBuilder(ClassName.get(activityClass.packageName, "On" + activityClass.simpleName + "ResultListener"))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(interfaceOnResultMethodBuilder.build())
                .build();
    }

    public ClassName getListenerClass(){
        return ClassName.get(activityClass.packageName + "." +activityClass.simpleName + "Builder", "On" + activityClass.simpleName + "ResultListener");
    }

    public com.squareup.kotlinpoet.LambdaTypeName getListenerClassKt() {
        //return new com.squareup.kotlinpoet.ClassName(packageName + "." + activityClass.simpleName + "Builder", "On" + activityClass.simpleName + "ResultListener");
        return onResultLambdaType;
    }

    public String getListenerName(){
        return "on" + activityClass.simpleName + "ResultListener";
    }

    public TypeSpec createOnResultListenerObject() {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(OnActivityResultListener.class)
                .addMethod(onResultMethodBuilder.build())
                .build();
    }

    public com.squareup.kotlinpoet.TypeSpec createOnResultListenerObjectKt() {
        return com.squareup.kotlinpoet.TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(OnActivityResultListener.class)
                .addFunction(onResultFunBuilderKt.build())
                .build();
    }

    public FunSpec buildFinishWithResultExt() {
        FunSpec.Builder funBuilder = FunSpec.builder("finishWithResult")
                .receiver(TypeNames.get(activityClass.getType().asType()));

        funBuilder.addStatement("val intent = %T()", KotlinTypes.INTENT);
        for (ResultEntity resultEntity : resultEntities) {
            TypeMirror typeMirror = null;
            try {
                resultEntity.type();
            } catch (MirroredTypeException e) {
                typeMirror = e.getTypeMirror();
            }
            funBuilder.addParameter(resultEntity.name(), KotlinTypes.toKotlinType(typeMirror));
            funBuilder.addStatement("intent.putExtra(%S, %L)", resultEntity.name(), resultEntity.name());
        }
        funBuilder.addStatement("setResult(1, intent)");
        funBuilder.addStatement("finish()");
        return funBuilder.build();
    }

    public MethodSpec buildFinishWithResultMethod() {
        return finishWithResultMethodBuilder.addStatement("activity.setResult(1, intent)").addStatement("activity.finish()").build();
    }
}
