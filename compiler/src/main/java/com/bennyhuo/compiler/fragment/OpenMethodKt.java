package com.bennyhuo.compiler.fragment;

import com.bennyhuo.compiler.basic.RequiredField;
import com.bennyhuo.compiler.utils.KotlinTypes;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.ParameterSpec;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;

import java.util.List;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */

public class OpenMethodKt {

    private String builderClassName;
    private FunSpec.Builder openExtFunBuilderForContext;
    private FunSpec.Builder openExtFunBuilderForViewGroup;
    private FunSpec.Builder openExtFunBuilderForFragment;
    private FragmentClass fragmentClass;

    public OpenMethodKt(FragmentClass fragmentClass, String builderClassName, String name) {

        this.fragmentClass = fragmentClass;
        this.builderClassName = builderClassName;
        openExtFunBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addParameter("containerId", TypeNames.INT)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T()", KotlinTypes.INTENT);

        openExtFunBuilderForViewGroup = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW_GROUP)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        openExtFunBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);
    }

    public void visitField(RequiredField binding) {
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if (!binding.isRequired()) {
            className = className.asNullable();
            openExtFunBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            openExtFunBuilderForContext.addParameter(name, className);
        }
        openExtFunBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void end() {
        openExtFunBuilderForContext
                .addStatement("val fragment = %T()", fragmentClass.getType())
                .addStatement("fragment.arguments = intent.getExtras()")
                .addStatement("supportFragmentManager.beginTransaction().replace(containerId, fragment).commit()")
                .addStatement("%T.inject()", new ClassName(fragmentClass.packageName, builderClassName));

        StringBuilder paramBuilder = new StringBuilder();
        List<ParameterSpec> parameterSpecList = openExtFunBuilderForContext.getParameters$kotlinpoet();
        for (int i = 1; i < parameterSpecList.size(); i++) {
            ParameterSpec parameterSpec = parameterSpecList.get(i);
            paramBuilder.append(parameterSpec.getName()).append(",");
            openExtFunBuilderForViewGroup.addParameter(parameterSpec);
            openExtFunBuilderForFragment.addParameter(parameterSpec);
        }
        if (paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        openExtFunBuilderForViewGroup.addStatement("(context as? %T)?.%L(id, %L)", KotlinTypes.SUPPORT_ACTIVITY, openExtFunBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
        openExtFunBuilderForFragment.addStatement("(view?.parent as? %T)?.%L(%L)", KotlinTypes.VIEW_GROUP, openExtFunBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
    }

    public FunSpec buildForContext() {
        return openExtFunBuilderForContext.build();
    }

    public FunSpec buildForView() {
        return openExtFunBuilderForViewGroup.build();
    }

    public FunSpec buildForFragment() {
        return openExtFunBuilderForFragment.build();
    }
}
