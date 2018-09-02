package com.bennyhuo.tieguanyin.compiler.activity;

import com.bennyhuo.tieguanyin.annotations.PendingTransition;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass;
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity;
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.ParameterSpec;
import com.squareup.kotlinpoet.TypeName;

import java.util.ArrayList;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */

public class StartFunctionKt {

    private String builderClassName;
    private FunSpec.Builder funBuilderForContext;
    private FunSpec.Builder funBuilderForView;
    private FunSpec.Builder funBuilderForFragment;
    private ActivityClass activityClass;

    public StartFunctionKt(ActivityClass activityClass, String builderClassName, String name) {

        this.activityClass = activityClass;
        this.builderClassName = builderClassName;
        funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(this, %T::class.java)", KotlinTypes.INTENT, activityClass.getType());

        funBuilderForView = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addStatement("%T.INSTANCE.init(context)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(context, %T::class.java)", KotlinTypes.INTENT, activityClass.getType());


        funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        for (String category : activityClass.getCategoriesRecursively()) {
            funBuilderForContext.addStatement("intent.addCategory(%S)", category);
            funBuilderForView.addStatement("intent.addCategory(%S)", category);
        }
        for (int flag: activityClass.getFlagsRecursively()) {
            funBuilderForContext.addStatement("intent.addFlags(%L)", flag);
            funBuilderForView.addStatement("intent.addFlags(%L)", flag);
        }
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if(!binding.isRequired()){
            className = className.asNullable();
            funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
            funBuilderForView.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            funBuilderForContext.addParameter(name, className);
            funBuilderForView.addParameter(name, className);
        }
        funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
        funBuilderForView.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        funBuilderForContext.addStatement("var options: %T? = null", KotlinTypes.BUNDLE);
        funBuilderForView.addStatement("var options: %T? = null", KotlinTypes.BUNDLE);

        ArrayList<SharedElementEntity> sharedElements = activityClass.getSharedElementsRecursively();
        if (sharedElements.size() > 0) {
            funBuilderForView.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.VIEW, KotlinTypes.STRING);

            funBuilderForContext.beginControlFlow("if(this is %T)", KotlinTypes.ACTIVITY);
            funBuilderForContext.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.VIEW, KotlinTypes.STRING);

            boolean firstNeedTransitionNameMap = true;
            for (SharedElementEntity sharedElement :sharedElements) {
                if(sharedElement.sourceId == 0){
                    if(firstNeedTransitionNameMap){
                        funBuilderForView.addStatement("val nameMap = %T<%T, %T>()", KotlinTypes.HASH_MAP, KotlinTypes.STRING, KotlinTypes.VIEW)
                        .addStatement("%T.findNamedViews(this, nameMap)", KotlinTypes.VIEW_UTILS);
                        funBuilderForContext.addStatement("val nameMap = %T<%T, %T>()", KotlinTypes.HASH_MAP, KotlinTypes.STRING, KotlinTypes.VIEW)
                                .addStatement("%T.findNamedViews(window.decorView, nameMap)", KotlinTypes.VIEW_UTILS);
                        firstNeedTransitionNameMap = false;
                    }

                    funBuilderForContext.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName);
                    funBuilderForView.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName);
                } else {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName);
                    funBuilderForView.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName);
                }
            }
            funBuilderForContext.addStatement("options = %T.makeSceneTransition(this, sharedElements)", KotlinTypes.ACTIVITY_BUILDER);
            funBuilderForContext.endControlFlow();

            funBuilderForView.addStatement("options = %T.makeSceneTransition(context, sharedElements)", KotlinTypes.ACTIVITY_BUILDER);
        }
        PendingTransition pendingTransition = activityClass.getPendingTransitionRecursively();
        if(activityResultClass != null){
            funBuilderForContext
                    .addStatement("%T.INSTANCE.startActivityForResult(this, intent, options, %L, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim(), activityResultClass.createOnResultListenerObjectKt())
                    .addParameter(
                            ParameterSpec.builder(activityResultClass.getListenerName(), activityResultClass.getListenerClassKt().asNullable())
                                    .defaultValue("null").build());
        } else {
            funBuilderForContext.addStatement("%T.INSTANCE.startActivity(this, intent, options, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim());
        }

        if(activityResultClass != null){
            funBuilderForView
                    .addStatement("%T.INSTANCE.startActivityForResult(context, intent, options, %L, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim(), activityResultClass.createOnResultListenerObjectKt())
                    .addParameter(
                            ParameterSpec.builder(activityResultClass.getListenerName(), activityResultClass.getListenerClassKt().asNullable())
                                    .defaultValue("null").build());
        } else {
            funBuilderForView.addStatement("%T.INSTANCE.startActivity(context, intent, options, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim());
        }

        StringBuilder paramBuilder = new StringBuilder();
        for (ParameterSpec parameterSpec : funBuilderForContext.getParameters()) {
            paramBuilder.append(parameterSpec.getName()).append(",");
            funBuilderForFragment.addParameter(parameterSpec);
        }
        if(paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        funBuilderForFragment.addStatement("view?.%L(%L)", funBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
    }

    public FunSpec buildForContext() {
        return funBuilderForContext.build();
    }

    public FunSpec buildForView() {
        return funBuilderForView.build();
    }

    public FunSpec buildForFragment() {
        return funBuilderForFragment.build();
    }
}
