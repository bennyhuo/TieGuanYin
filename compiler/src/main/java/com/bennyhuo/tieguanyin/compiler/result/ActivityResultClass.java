package com.bennyhuo.tieguanyin.compiler.result;

import com.bennyhuo.tieguanyin.annotations.ResultEntity;
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.LambdaTypeName;
import com.squareup.kotlinpoet.ParameterSpec;
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

    private ActivityClass activityClass;

    public ActivityResultClass(ActivityClass activityClass, ResultEntity[] resultEntities) {
        this.activityClass = activityClass;
        this.resultEntities = resultEntities;
    }

    /**
     * @return onSampleActivityResultListener(int a, String b)
     */
    public TypeSpec buildOnActivityResultListenerInterface() {
        MethodSpec.Builder interfaceOnResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(TypeName.VOID);

        for (ResultEntity resultEntity : resultEntities) {
            TypeName resultClass = ClassName.get(getResultType(resultEntity));
            interfaceOnResultMethodBuilder.addParameter(resultClass, resultEntity.name());
        }

        return TypeSpec.interfaceBuilder(getListenerClass())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(interfaceOnResultMethodBuilder.build())
                .build();
    }

    public ClassName getListenerClass(){
        return ClassName.get(activityClass.packageName + "." +activityClass.simpleName + "Builder", "On" + activityClass.simpleName + "ResultListener");
    }

    public com.squareup.kotlinpoet.LambdaTypeName getListenerClassKt() {
        ArrayList<ParameterSpec> argTypeNames = new ArrayList<>();
        for (ResultEntity resultEntity : resultEntities) {
            argTypeNames.add(ParameterSpec.builder(resultEntity.name(), KotlinTypes.toKotlinType(getResultType(resultEntity))).build());
        }
        return LambdaTypeName.get(null, argTypeNames, TypeNames.get(Unit.class));
    }

    /**
     * @return onSampleActivityResultListener
     */
    public String getListenerName(){
        return "on" + activityClass.simpleName + "ResultListener";
    }

    public TypeSpec createOnResultListenerObject() {
        MethodSpec.Builder onResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JavaTypes.BUNDLE, "bundle")
                .returns(TypeName.VOID);

        onResultMethodBuilder.beginControlFlow("if($L != null)", getListenerName());
        StringBuilder statementBuilder = new StringBuilder();

        ArrayList<Object> args = new ArrayList<>();
        args.add(getListenerName());
        for (ResultEntity resultEntity : resultEntities) {
            TypeName resultClass = ClassName.get(getResultType(resultEntity));
            statementBuilder.append("$T.<$T>get(bundle, $S),");
            args.add(JavaTypes.RUNTIME_UTILS);
            args.add(resultClass.box());
            args.add(resultEntity.name());
        }
        if (statementBuilder.length() > 0){
            statementBuilder.deleteCharAt(statementBuilder.length() - 1);
        }
        onResultMethodBuilder.addStatement("$L.onResult(" + statementBuilder.toString() + ")", args.toArray());
        onResultMethodBuilder.endControlFlow();

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(JavaTypes.ON_ACTIVITY_RESULT_LISTENER)
                .addMethod(onResultMethodBuilder.build())
                .build();
    }

    /**
     * @return object: onSampleActivityResultListener{ override fun onResult(bundle: Bundle){ if(not null) invoke. } }
     */
    public com.squareup.kotlinpoet.TypeSpec createOnResultListenerObjectKt() {
        FunSpec.Builder onResultFunBuilderKt =FunSpec.builder("onResult")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", KotlinTypes.BUNDLE)
                .returns(TypeNames.UNIT);

        onResultFunBuilderKt.beginControlFlow("if(%L != null)", getListenerName());
        StringBuilder statementBuilderKt = new StringBuilder();
        ArrayList<Object> argsKt = new ArrayList<>();
        argsKt.add(getListenerName());

        for (ResultEntity resultEntity : resultEntities) {
            statementBuilderKt.append("%T.get(bundle, %S),");
            argsKt.add(KotlinTypes.RUNTIME_UTILS);
            argsKt.add(resultEntity.name());
        }
        if(statementBuilderKt.length() > 0) {
            statementBuilderKt.deleteCharAt(statementBuilderKt.length() - 1);
        }
        onResultFunBuilderKt.addStatement("%L(" + statementBuilderKt.toString() + ")", argsKt.toArray());
        onResultFunBuilderKt.endControlFlow();

        return com.squareup.kotlinpoet.TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(KotlinTypes.ON_ACTIVITY_RESULT_LISTENER)
                .addFunction(onResultFunBuilderKt.build())
                .build();
    }

    public FunSpec buildFinishWithResultKt() {
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
        MethodSpec.Builder finishWithResultMethodBuilder = MethodSpec.methodBuilder("finishWithResult")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(ClassName.get(activityClass.getType()), "activity")
                .returns(TypeName.VOID)
                .addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);

        for (ResultEntity resultEntity : resultEntities) {
            finishWithResultMethodBuilder.addParameter(ClassName.get(getResultType(resultEntity)), resultEntity.name());
            finishWithResultMethodBuilder.addStatement("intent.putExtra($S, $L)", resultEntity.name(), resultEntity.name());
        }
        return finishWithResultMethodBuilder.addStatement("activity.setResult(1, intent)").addStatement("activity.finish()").build();
    }

    public ResultEntity[] getResultEntities(){
        return resultEntities;
    }

    public static TypeMirror getResultType(ResultEntity entity){
        TypeMirror typeMirror = null;
        try {
            entity.type();
        } catch (MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
        }
        return typeMirror;
    }
}
