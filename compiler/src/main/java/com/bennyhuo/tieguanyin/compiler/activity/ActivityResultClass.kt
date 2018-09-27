package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.basic.entity.ResultParameter
import com.bennyhuo.tieguanyin.compiler.basic.entity.asResultParameter
import com.bennyhuo.tieguanyin.compiler.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.basic.types.ON_ACTIVITY_RESULT_LISTENER
import com.bennyhuo.tieguanyin.compiler.basic.types.RUNTIME_UTILS
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.*
import java.util.*
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */
class ActivityResultClass(private val activityClass: ActivityClass, resultEntities: Array<ResultEntity>) {

    private val declaredResultParameters = resultEntities.mapTo(TreeSet(), ResultEntity::asResultParameter)

    var resultParameters: Set<ResultParameter> = declaredResultParameters
        private set

    var superActivityResultClass: ActivityResultClass? = null
        set(value) {
            field = value
            value?.let {
                resultParameters += it.resultParameters
            }
        }

    val listenerClass = ClassName.get(activityClass.packageName, activityClass.builderClassName,
            "On" + activityClass.simpleName + "ResultListener")

    val listenerClassKt by lazy {
        this.resultParameters.map { resultEntity ->
            ParameterSpec.builder(resultEntity.name, resultEntity.kotlinTypeName).build()
        }.let { LambdaTypeName.get(null, it, UNIT).asNullable() }
    }
    /**
     * @return literal name like "onSampleActivityResultListener"
     */
    val listenerName = "on" + activityClass.simpleName + "ResultListener"

    /**
     * @return literal like "onSampleActivityResultListener(int a, String b)"
     */
    fun buildOnActivityResultListenerInterface(): TypeSpec {
        val interfaceOnResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(TypeName.VOID)

        resultParameters.forEach { resultEntity ->
            interfaceOnResultMethodBuilder.addParameter(resultEntity.javaTypeName, resultEntity.name)
        }

        return TypeSpec.interfaceBuilder(listenerClass)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(interfaceOnResultMethodBuilder.build())
                .build()
    }

    fun createOnResultListenerObject(): TypeSpec {
        val onResultMethodBuilder = MethodSpec.methodBuilder("onResult")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(BUNDLE.java, "bundle")
                .returns(TypeName.VOID)

        onResultMethodBuilder.beginControlFlow("if(\$L != null)", listenerName)
        val statementBuilder = StringBuilder()

        val args = ArrayList<Any>()
        args.add(listenerName)
        for (resultEntity in resultParameters) {
            statementBuilder.append("\$T.<\$T>get(bundle, \$S),")
            args.add(RUNTIME_UTILS.java)
            args.add(resultEntity.javaTypeName.box())
            args.add(resultEntity.name)
        }
        if (statementBuilder.isNotEmpty()) {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }
        onResultMethodBuilder.addStatement("\$L.onResult(" + statementBuilder.toString() + ")", *args.toTypedArray())
        onResultMethodBuilder.endControlFlow()

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ON_ACTIVITY_RESULT_LISTENER.java)
                .addMethod(onResultMethodBuilder.build())
                .build()
    }

    /**
     * @return object: onSampleActivityResultListener{ override fun onResult(bundle: Bundle){ if(not null) invoke. } }
     */
    fun createOnResultListenerObjectKt(): com.squareup.kotlinpoet.TypeSpec {
        val onResultFunBuilderKt = FunSpec.builder("onResult")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", BUNDLE.kotlin)
                .returns(UNIT)

        onResultFunBuilderKt.beginControlFlow("if(%L != null)", listenerName)
        val statementBuilderKt = StringBuilder()
        val argsKt = ArrayList<Any>()
        argsKt.add(listenerName)

        for (resultEntity in resultParameters) {
            statementBuilderKt.append("%T.get(bundle, %S),")
            argsKt.add(RUNTIME_UTILS.kotlin)
            argsKt.add(resultEntity.name)
        }
        if (statementBuilderKt.isNotEmpty()) {
            statementBuilderKt.deleteCharAt(statementBuilderKt.length - 1)
        }
        onResultFunBuilderKt.addStatement("%L(" + statementBuilderKt.toString() + ")", *argsKt.toTypedArray())
        onResultFunBuilderKt.endControlFlow()

        return com.squareup.kotlinpoet.TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ON_ACTIVITY_RESULT_LISTENER.kotlin, CodeBlock.of(""))
                .addFunction(onResultFunBuilderKt.build())
                .build()
    }
}
