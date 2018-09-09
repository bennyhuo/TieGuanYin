package com.bennyhuo.tieguanyin.compiler.fragment.methods;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class SaveStateMethodBuilder {

    private FragmentClass fragmentClass;

    public SaveStateMethodBuilder(FragmentClass fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public void build(TypeSpec.Builder typeBuilder){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("saveState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.SUPPORT_FRAGMENT, "fragment")
                .addParameter(JavaTypes.BUNDLE, "outState")
                .beginControlFlow("if(fragment instanceof $T)", fragmentClass.getType())
                .addStatement("$T typedFragment = ($T) fragment", fragmentClass.getType(), fragmentClass.getType());

        methodBuilder.addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);

        for (RequiredField requiredField : fragmentClass.getRequiredFieldsRecursively()) {
            String name = requiredField.getName();
            if(requiredField.isPrivate()){
                methodBuilder.addStatement("intent.putExtra($S, typedFragment.get$L())", name, Utils.capitalize(name));
            } else {
                methodBuilder.addStatement("intent.putExtra($S, typedFragment.$L)", name, name);
            }
        }

        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow();

        typeBuilder.addMethod(methodBuilder.build());
    }

}
