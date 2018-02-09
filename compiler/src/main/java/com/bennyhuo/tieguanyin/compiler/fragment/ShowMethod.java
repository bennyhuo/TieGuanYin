package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
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

public class ShowMethod {

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
    private FragmentClass fragmentClass;
    private ArrayList<RequiredField> visitedBindings = new ArrayList<>();

    public ShowMethod(FragmentClass fragmentClass, String name) {
        this.fragmentClass = fragmentClass;
        methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(int.class, "containerId")
                .beginControlFlow("if(activity instanceof $T)", JavaTypes.SUPPORT_ACTIVITY)
                .addStatement("$T.INSTANCE.init(activity)", JavaTypes.ACTIVITY_BUILDER);

        methodBuilder.addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);
    }

    public void visitField(RequiredField binding) {
        String name = binding.getName();
        methodBuilder.addParameter(ClassName.get(binding.getSymbol().type), name);
        methodBuilder.addStatement("intent.putExtra($S, $L)", name, name);
        visitedBindings.add(binding);
    }

    public void end() {
        methodBuilder.addStatement("$T fragmentActivity = ($T) activity", JavaTypes.SUPPORT_ACTIVITY, JavaTypes.SUPPORT_ACTIVITY)
                .addStatement("$T fragment = new $T()", fragmentClass.getType(), fragmentClass.getType())
                .addStatement("fragment.setArguments(intent.getExtras())")
                .addStatement("fragmentActivity.getSupportFragmentManager().beginTransaction().replace(containerId, fragment).commit()")
                .endControlFlow();
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }

    public void renameTo(String newName) {
        try {
            field.set(methodBuilder, newName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ShowMethod copy(String name) {
        ShowMethod openMethod = new ShowMethod(fragmentClass, name);
        for (RequiredField visitedBinding : visitedBindings) {
            openMethod.visitField(visitedBinding);
        }
        return openMethod;
    }
}
