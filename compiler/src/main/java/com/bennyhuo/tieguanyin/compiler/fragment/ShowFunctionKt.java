package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes;
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

public class ShowFunctionKt {

    private String builderClassName;
    private FunSpec.Builder funBuilderForContext;
    private FunSpec.Builder funBuilderForViewGroup;
    private FunSpec.Builder funBuilderForFragment;
    private FragmentClass fragmentClass;

    public ShowFunctionKt(FragmentClass fragmentClass, String builderClassName, String name) {

        this.fragmentClass = fragmentClass;
        this.builderClassName = builderClassName;
        funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addParameter("containerId", TypeNames.INT)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T()", KotlinTypes.INTENT);

        funBuilderForViewGroup = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW_GROUP)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);
    }

    public void visitField(RequiredField binding) {
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if (!binding.isRequired()) {
            className = className.asNullable();
            funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            funBuilderForContext.addParameter(name, className);
        }
        funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void end() {
        funBuilderForContext.addStatement("%T.showFragment(this, containerId, intent.getExtras(), %T::class.java)", KotlinTypes.FRAGMENT_BUILDER,  fragmentClass.getType());

        StringBuilder paramBuilder = new StringBuilder();
        List<ParameterSpec> parameterSpecList = funBuilderForContext.getParameters$kotlinpoet();
        for (int i = 1; i < parameterSpecList.size(); i++) {
            ParameterSpec parameterSpec = parameterSpecList.get(i);
            paramBuilder.append(parameterSpec.getName()).append(",");
            funBuilderForViewGroup.addParameter(parameterSpec);
            funBuilderForFragment.addParameter(parameterSpec);
        }
        if (paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        funBuilderForFragment.addStatement("(view?.parent as? %T)?.%L(%L)", KotlinTypes.VIEW_GROUP, funBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
        if(paramBuilder.length() > 0){
            paramBuilder.insert(0, ',');
        }
        funBuilderForViewGroup.addStatement("(context as? %T)?.%L(id %L)", KotlinTypes.SUPPORT_ACTIVITY, funBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
    }

    public FunSpec buildForContext() {
        return funBuilderForContext.build();
    }

    public FunSpec buildForView() {
        return funBuilderForViewGroup.build();
    }

    public FunSpec buildForFragment() {
        return funBuilderForFragment.build();
    }
}
