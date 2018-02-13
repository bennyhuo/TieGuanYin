package com.bennyhuo.tieguanyin.compiler.activity;

import com.bennyhuo.tieguanyin.annotations.SharedElement;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class StartMethod {

    private static Field field;
    static {
        try {
            field = MethodSpec.Builder.class.getDeclaredField("name");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec.Builder methodBuilder;
    private MethodSpec.Builder methodBuilderForView;
    private ActivityClass activityClass;
    private ArrayList<RequiredField> visitedBindings = new ArrayList<>();

    public StartMethod(ActivityClass activityClass, String name) {
        this.activityClass = activityClass;
        methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.CONTEXT, "context")
                .addStatement("$T.INSTANCE.init(context)", JavaTypes.ACTIVITY_BUILDER);

        methodBuilder.addStatement("$T intent = new $T(context, $T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.getType());

        methodBuilderForView = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.VIEW, "view")
                .addStatement("$T.INSTANCE.init(view.getContext())", JavaTypes.ACTIVITY_BUILDER);

        methodBuilderForView.addStatement("$T intent = new $T(view.getContext(), $T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.getType());
    }

    public void visitField(RequiredField requiredField){
        String name = requiredField.getName();
        methodBuilder.addParameter(ClassName.get(requiredField.getSymbol().type), name);
        methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);

        methodBuilderForView.addParameter(ClassName.get(requiredField.getSymbol().type), name);
        methodBuilderForView.addStatement("intent.putExtra($S, $L)", name, name);

        visitedBindings.add(requiredField);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        methodBuilder.addStatement("$T options = null", JavaTypes.BUNDLE);
        methodBuilderForView.addStatement("$T options = null", JavaTypes.BUNDLE);
        ArrayList<SharedElement> sharedElements = activityClass.getSharedElementsRecursively();
        if (sharedElements.size() > 0) {
            methodBuilderForView.addStatement("$T<$T<$T, $T>> sharedElements = new $T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String.class, JavaTypes.ARRAY_LIST);

            methodBuilder.beginControlFlow("if(context instanceof $T)", JavaTypes.ACTIVITY)
                    .addStatement("$T activity = ($T) context", JavaTypes.ACTIVITY, JavaTypes.ACTIVITY)
                    .addStatement("$T<$T<$T, $T>> sharedElements = new $T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String.class, JavaTypes.ARRAY_LIST);

            for (SharedElement sharedElement : sharedElements) {
                methodBuilder.addStatement("sharedElements.add(new Pair<>(activity.findViewById($L), $S))", sharedElement.sourceId(), sharedElement.targetName());
                methodBuilderForView.addStatement("sharedElements.add(new Pair<>(view.findViewById($L), $S))", sharedElement.sourceId(), sharedElement.targetName());
            }
            methodBuilderForView.addStatement("options = $T.makeSceneTransition(view.getContext(), sharedElements)", JavaTypes.ACTIVITY_BUILDER);

            methodBuilder.addStatement("options = $T.makeSceneTransition(context, sharedElements)", JavaTypes.ACTIVITY_BUILDER)
                    .endControlFlow();
        }
        if(activityResultClass != null){
            methodBuilder.addStatement("$T.INSTANCE.startActivityForResult(context, intent, options, $L)", JavaTypes.ACTIVITY_BUILDER, activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilder.addStatement("$T.INSTANCE.startActivity(context, intent, options)", JavaTypes.ACTIVITY_BUILDER);
        }

        if (activityResultClass != null) {
            methodBuilderForView.addStatement("$T.INSTANCE.startActivityForResult(view.getContext(), intent, options, $L)", JavaTypes.ACTIVITY_BUILDER, activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilderForView.addStatement("$T.INSTANCE.startActivity(view.getContext(), intent, options)", JavaTypes.ACTIVITY_BUILDER);
        }
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }

    public MethodSpec buildForView() {
        return methodBuilderForView.build();
    }

    public void renameTo(String newName){
        try {
            field.set(methodBuilder, newName);
            field.set(methodBuilderForView, newName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StartMethod copy(String name){
        StartMethod openMethod = new StartMethod(activityClass, name);
        for (RequiredField visitedBinding : visitedBindings) {
            openMethod.visitField(visitedBinding);
        }
        return openMethod;
    }
}
