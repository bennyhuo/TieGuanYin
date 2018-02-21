package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity;
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
        ArrayList<SharedElementEntity> sharedElements = fragmentClass.getSharedElementsRecursively();
        if(sharedElements.isEmpty()){
            methodBuilder.addStatement("$T.showFragment(($T) activity, containerId, intent.getExtras(), $T.class, null)", JavaTypes.FRAGMENT_BUILDER,  JavaTypes.SUPPORT_ACTIVITY, fragmentClass.getType());
        } else {
            methodBuilder.addStatement("$T<$T<$T, $T>> sharedElements = new $T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, String.class, String.class, JavaTypes.ARRAY_LIST)
                    .addStatement("$T container = activity.findViewById(containerId)", JavaTypes.VIEW);
            for (SharedElementEntity sharedElement : sharedElements) {
                if(sharedElement.sourceId == 0){
                    methodBuilder.addStatement("sharedElements.add(new Pair<>($S, $S))", sharedElement.sourceName, sharedElement.targetName);
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>($T.getTransitionName(container.findViewById($L)), $S))", JavaTypes.VIEW_COMPAT, sharedElement.sourceId, sharedElement.targetName);
                }
            }
            methodBuilder.addStatement("$T.showFragment(($T) activity, containerId, intent.getExtras(), $T.class, sharedElements)", JavaTypes.FRAGMENT_BUILDER,  JavaTypes.SUPPORT_ACTIVITY, fragmentClass.getType());
        }
        methodBuilder.endControlFlow();
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
