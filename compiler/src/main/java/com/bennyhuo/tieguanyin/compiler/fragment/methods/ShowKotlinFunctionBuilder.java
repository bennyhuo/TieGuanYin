package com.bennyhuo.tieguanyin.compiler.fragment.methods;

import com.bennyhuo.tieguanyin.compiler.basic.OptionalField;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass;
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClassBuilder;
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity;
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.ParameterSpec;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */

public class ShowKotlinFunctionBuilder {

    private FragmentClass fragmentClass;
    private String name;

    public ShowKotlinFunctionBuilder(FragmentClass fragmentClass) {
        this.fragmentClass = fragmentClass;
        this.name = FragmentClassBuilder.METHOD_NAME + fragmentClass.getSimpleName();
    }

    public void build(FileSpec.Builder fileBuilder){
        FunSpec.Builder funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addParameter("containerId", TypeNames.INT)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T()", KotlinTypes.INTENT);

        FunSpec.Builder funBuilderForViewGroup = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW_GROUP)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        FunSpec.Builder funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        for (RequiredField requiredField : fragmentClass.getRequiredFieldsRecursively()) {
            String name = requiredField.getName();
            TypeName className = KotlinTypes.toKotlinType(requiredField.getSymbol().type);
            if (requiredField instanceof OptionalField) {
                className = className.asNullable();
                funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
            } else {
                funBuilderForContext.addParameter(name, className);
            }
            funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
        }

        ArrayList<SharedElementEntity> sharedElements = fragmentClass.getSharedElementsRecursively();
        if(sharedElements.isEmpty()) {
            funBuilderForContext.addStatement("%T.showFragment(this, containerId, intent.getExtras(), %T::class.java, null)", KotlinTypes.FRAGMENT_BUILDER, fragmentClass.getType());
        } else {
            funBuilderForContext.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.STRING, KotlinTypes.STRING)
                    .addStatement("val container: %T = findViewById(containerId)", KotlinTypes.VIEW);
            for (SharedElementEntity sharedElement : sharedElements) {
                if(sharedElement.sourceId == 0){
                    funBuilderForContext.addStatement("sharedElements.add(Pair(%S, %S))", sharedElement.sourceName, sharedElement.targetName);
                } else {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(%T.getTransitionName(container.findViewById(%L)), %S))", KotlinTypes.VIEW_COMPAT, sharedElement.sourceId, sharedElement.targetName);
                }
            }
            funBuilderForContext.addStatement("%T.showFragment(this, containerId, intent.getExtras(), %T::class.java, sharedElements)", KotlinTypes.FRAGMENT_BUILDER,  fragmentClass.getType());
        }
        StringBuilder paramBuilder = new StringBuilder();
        List<ParameterSpec> parameterSpecList = funBuilderForContext.getParameters();
        for (int i = 1; i < parameterSpecList.size(); i++) {
            ParameterSpec parameterSpec = parameterSpecList.get(i);
            paramBuilder.append(parameterSpec.getName()).append(",");
            funBuilderForViewGroup.addParameter(parameterSpec);
            funBuilderForFragment.addParameter(parameterSpec);
        }
        if (paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        funBuilderForFragment.addStatement("(view?.parent as? %T)?.%L(%L)", KotlinTypes.VIEW_GROUP, name, paramBuilder.toString());
        if(paramBuilder.length() > 0){
            paramBuilder.insert(0, ',');
        }
        funBuilderForViewGroup.addStatement("(context as? %T)?.%L(id %L)", KotlinTypes.SUPPORT_ACTIVITY, name, paramBuilder.toString());

        fileBuilder.addFunction(funBuilderForContext.build());
        fileBuilder.addFunction(funBuilderForViewGroup.build());
        fileBuilder.addFunction(funBuilderForFragment.build());
    }
}
