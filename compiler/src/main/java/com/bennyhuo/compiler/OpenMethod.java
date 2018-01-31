package com.bennyhuo.compiler;

import com.bennyhuo.activitybuilder.ActivityBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class OpenMethod {

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
    private ActivityClass activityClass;
    private ArrayList<RequiredField> visitedBindings = new ArrayList<>();

    public OpenMethod(ActivityClass activityClass, String name) {
        this.activityClass = activityClass;
        methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addStatement("$T.INSTANCE.init(context)", ActivityBuilder.class);

        ClassName intentClass = ClassName.get("android.content", "Intent");
        methodBuilder.addStatement("$T intent = new $T(context, $T.class)", intentClass, intentClass, activityClass.getType());
    }

    public void visitBinding(RequiredField binding){
        String name = binding.getName();
        methodBuilder.addParameter(ClassName.get(binding.getSymbol().type), name);
        methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);
        visitedBindings.add(binding);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        if(activityResultClass != null){
            ClassName activityClassName = ClassName.get("android.app", "Activity");
            methodBuilder.beginControlFlow("if(context instanceof $T)", activityClassName)
                    .addStatement("$T.INSTANCE.startActivityForResult(($T) context, intent, $L)", ActivityBuilder.class, activityClassName, activityResultClass.createOnResultListenerObject())
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("context.startActivity(intent)")
                    .endControlFlow();

            methodBuilder.addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilder.addStatement("context.startActivity(intent)");
        }
        methodBuilder.addStatement("inject()");
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }

    public void renameTo(String newName){
        try {
            field.set(methodBuilder, newName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OpenMethod copy(String name){
        OpenMethod openMethod = new OpenMethod(activityClass, name);
        for (RequiredField visitedBinding : visitedBindings) {
            openMethod.visitBinding(visitedBinding);
        }
        return openMethod;
    }
}
