package com.bennyhuo.tieguanyin.compiler.activity;

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
    }

    public void visitField(RequiredField requiredField){
        String name = requiredField.getName();
        methodBuilder.addParameter(ClassName.get(requiredField.getSymbol().type), name);
        methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);
        visitedBindings.add(requiredField);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        methodBuilder.beginControlFlow("if(context instanceof $T)", JavaTypes.ACTIVITY);
        if(activityResultClass != null){
            methodBuilder.beginControlFlow("if($N == null)", activityResultClass.getListenerName())
                    .addStatement("(($T)context).startActivityForResult(intent, 1)", JavaTypes.ACTIVITY)
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("$T.INSTANCE.startActivityForResult(($T) context, intent, $L)", JavaTypes.ACTIVITY_BUILDER, JavaTypes.ACTIVITY, activityResultClass.createOnResultListenerObject())
                    .endControlFlow()
                    .addParameter(activityResultClass.getListenerClass(), activityResultClass.getListenerName(), Modifier.FINAL);
        } else {
            methodBuilder.addStatement("context.startActivity(intent)");
        }
        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("intent.addFlags($T.FLAG_ACTIVITY_NEW_TASK)", JavaTypes.INTENT)
                .addStatement("context.startActivity(intent)")
                .endControlFlow();
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

    public StartMethod copy(String name){
        StartMethod openMethod = new StartMethod(activityClass, name);
        for (RequiredField visitedBinding : visitedBindings) {
            openMethod.visitField(visitedBinding);
        }
        return openMethod;
    }
}
