package com.bennyhuo.tieguanyin.compiler.activity.methods;

import com.bennyhuo.tieguanyin.annotations.PendingTransition;
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass;
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class StartMethod {
    private ActivityClass activityClass;
    private ArrayList<RequiredField> requiredFields = new ArrayList<>();
    private String name;
    private boolean isStaticMethod = true;

    public StartMethod(ActivityClass activityClass, String name) {
        this.activityClass = activityClass;
        this.name = name;
    }

    public void visitField(RequiredField requiredField){
        requiredFields.add(requiredField);
    }

    public StartMethod staticMethod(boolean staticMethod) {
        isStaticMethod = staticMethod;
        return this;
    }

    public StartMethod copy(String name){
        StartMethod openMethod = new StartMethod(activityClass, name);
        openMethod.requiredFields.addAll(this.requiredFields);
        return openMethod;
    }

    public void brew(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.CONTEXT, "context")
                .addStatement("$T.INSTANCE.init(context)", JavaTypes.ACTIVITY_BUILDER);

        methodBuilder.addStatement("$T intent = new $T(context, $T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.getType());

        MethodSpec.Builder methodBuilderForView = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.VIEW, "view")
                .addStatement("$T.INSTANCE.init(view.getContext())", JavaTypes.ACTIVITY_BUILDER);

        methodBuilderForView.addStatement("$T intent = new $T(view.getContext(), $T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.getType());
        for (String category : activityClass.getCategoriesRecursively()) {
            methodBuilder.addStatement("intent.addCategory($S)", category);
            methodBuilderForView.addStatement("intent.addCategory($S)", category);
        }
        for (int flag : activityClass.getFlagsRecursively()) {
            methodBuilder.addStatement("intent.addFlags($L)", flag);
            methodBuilderForView.addStatement("intent.addFlags($L)", flag);
        }

        for (RequiredField requiredField : requiredFields) {
            String name = requiredField.getName();
            methodBuilder.addParameter(ClassName.get(requiredField.getSymbol().type), name);
            methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);

            methodBuilderForView.addParameter(ClassName.get(requiredField.getSymbol().type), name);
            methodBuilderForView.addStatement("intent.putExtra($S, $L)", name, name);
        }

        if(isStaticMethod){
            methodBuilder.addModifiers(Modifier.STATIC);
            methodBuilderForView.addModifiers(Modifier.STATIC);
        } else {
            //非静态则需要填充 optional 成员
            methodBuilder.addStatement("fillIntent(intent)");
            methodBuilderForView.addStatement("fillIntent(intent)");
        }

        methodBuilder.addStatement("$T options = null", JavaTypes.BUNDLE);
        methodBuilderForView.addStatement("$T options = null", JavaTypes.BUNDLE);
        ArrayList<SharedElementEntity> sharedElements = activityClass.getSharedElementsRecursively();
        if (sharedElements.size() > 0) {
            methodBuilderForView.addStatement("$T<$T<$T, $T>> sharedElements = new $T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String.class, JavaTypes.ARRAY_LIST);

            methodBuilder.beginControlFlow("if(context instanceof $T)", JavaTypes.ACTIVITY)
                    .addStatement("$T activity = ($T) context", JavaTypes.ACTIVITY, JavaTypes.ACTIVITY)
                    .addStatement("$T<$T<$T, $T>> sharedElements = new $T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String.class, JavaTypes.ARRAY_LIST);

            boolean firstNeedTransitionNameMap = true;
            for (SharedElementEntity sharedElement : sharedElements) {
                if (sharedElement.sourceId == 0) {
                    if (firstNeedTransitionNameMap) {
                        methodBuilderForView.addStatement("$T<$T, $T> nameMap = new $T<>()", JavaTypes.HASH_MAP, String.class, JavaTypes.VIEW, JavaTypes.HASH_MAP)
                                .addStatement("$T.findNamedViews(view, nameMap)", JavaTypes.VIEW_UTILS);
                        methodBuilder.addStatement("$T<$T, $T> nameMap = new $T<>()", JavaTypes.HASH_MAP, String.class, JavaTypes.VIEW, JavaTypes.HASH_MAP)
                                .addStatement("$T.findNamedViews(activity.getWindow().getDecorView(), nameMap)", JavaTypes.VIEW_UTILS);
                        firstNeedTransitionNameMap = false;
                    }

                    methodBuilder.addStatement("sharedElements.add(new Pair<>(nameMap.get($S), $S))", sharedElement.sourceName, sharedElement.targetName);
                    methodBuilderForView.addStatement("sharedElements.add(new Pair<>(nameMap.get($S), $S))", sharedElement.sourceName, sharedElement.targetName);
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(activity.findViewById($L), $S))", sharedElement.sourceId, sharedElement.targetName);
                    methodBuilderForView.addStatement("sharedElements.add(new Pair<>(view.findViewById($L), $S))", sharedElement.sourceId, sharedElement.targetName);
                }
            }

            methodBuilderForView.addStatement("options = $T.makeSceneTransition(view.getContext(), sharedElements)", JavaTypes.ACTIVITY_BUILDER);

            methodBuilder.addStatement("options = $T.makeSceneTransition(context, sharedElements)", JavaTypes.ACTIVITY_BUILDER)
                    .endControlFlow();
        }

        PendingTransition pendingTransition = activityClass.getPendingTransitionRecursively();
        ActivityResultClass activityResultClass = activityClass.getActivityResultClass();
        if (activityResultClass != null) {
            methodBuilder.addStatement("$T.INSTANCE.startActivityForResult(context, intent, options, $L, $L, $L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim(), activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilder.addStatement("$T.INSTANCE.startActivity(context, intent, options, $L, $L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim());
        }

        if (activityResultClass != null) {
            methodBuilderForView.addStatement("$T.INSTANCE.startActivityForResult(view.getContext(), intent, options, $L, $L, $L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim(), activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilderForView.addStatement("$T.INSTANCE.startActivity(view.getContext(), intent, options, $L, $L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim(), pendingTransition.exitAnim());
        }


        typeBuilder.addMethod(methodBuilder.build());
        typeBuilder.addMethod(methodBuilderForView.build());
    }
}
