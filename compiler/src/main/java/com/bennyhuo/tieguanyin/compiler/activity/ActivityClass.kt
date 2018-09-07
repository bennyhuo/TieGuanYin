package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.annotations.GenerateMode.Auto
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.compiler.activity.methods.InjectMethod
import com.bennyhuo.tieguanyin.compiler.activity.methods.MethodsBuilder
import com.bennyhuo.tieguanyin.compiler.activity.methods.SaveStateMethod
import com.bennyhuo.tieguanyin.compiler.activity.methods.StartFunctionKt
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField
import com.bennyhuo.tieguanyin.compiler.extensions.isDefault
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import com.bennyhuo.tieguanyin.compiler.utils.Utils
import com.squareup.javapoet.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import com.sun.tools.javac.code.Type
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.StandardLocation

/**
 * Created by benny on 1/29/18.
 */

class ActivityClass(val type: TypeElement) {

    val simpleName: String = TypeUtils.simpleName(type.asType())
    val packageName: String = TypeUtils.getPackageName(type)

    private val requiredFields = TreeSet<RequiredField>()

    private val pendingTransition: PendingTransition
    private val sharedElements = ArrayList<SharedElementEntity>()
    private val pendingTransitionOnFinish: PendingTransition
    private val categories = ArrayList<String>()
    private val flags = ArrayList<Int>()

    private var superActivityClass: ActivityClass? = null
    var activityResultClass: ActivityResultClass? = null
    private var generateMode: GenerateMode

    init {
        val generateBuilder = type.getAnnotation(ActivityBuilder::class.java)
        if (generateBuilder.resultTypes.isNotEmpty()) {
            activityResultClass = ActivityResultClass(this, generateBuilder.resultTypes)
        }

        generateMode = generateBuilder.mode
        if (generateMode == GenerateMode.Auto) {
            //如果有这个注解，说明就是 Kotlin 类
            val isKotlin = type.getAnnotation(META_DATA) != null
            generateMode = if (isKotlin) GenerateMode.Both else GenerateMode.JavaOnly
        }

        generateBuilder.sharedElements.mapTo(sharedElements){ SharedElementEntity(it) }
        generateBuilder.sharedElementsByNames.mapTo(sharedElements){ SharedElementEntity(it) }
        generateBuilder.sharedElementsWithName.mapTo(sharedElements){ SharedElementEntity(it) }

        flags.addAll(generateBuilder.flags.asList())
        categories.addAll(generateBuilder.categories)

        pendingTransition = generateBuilder.pendingTransition
        pendingTransitionOnFinish = generateBuilder.pendingTransitionOnFinish
    }


    val requiredFieldsRecursively: TreeSet<RequiredField> by lazy {
        superActivityClass?.let {
            TreeSet<RequiredField>(requiredFields)
                    .apply {
                        addAll(it.requiredFieldsRecursively)
                    }
        } ?: requiredFields
    }

    private var sharedElementsRecursively: ArrayList<SharedElementEntity>? = null

    fun getSharedElementsRecursively(): ArrayList<SharedElementEntity> {
        if (superActivityClass == null) {
            return sharedElements
        }
        if (sharedElementsRecursively == null) {
            sharedElementsRecursively = ArrayList(sharedElements)
            sharedElementsRecursively!!.addAll(superActivityClass!!.getSharedElementsRecursively())
        }
        return sharedElementsRecursively as ArrayList<SharedElementEntity>
    }

    val pendingTransitionRecursively: PendingTransition by lazy {
        superActivityClass?.let {
            if(pendingTransition.isDefault()){
                it.pendingTransitionRecursively
            } else {
                pendingTransition
            }
        }?: pendingTransition
    }

    val pendingTransitionOnFinishRecursively: PendingTransition by lazy {
        superActivityClass?.let {
            if(pendingTransitionOnFinish.isDefault()){
                it.pendingTransitionOnFinishRecursively
            } else {
                pendingTransitionOnFinish
            }
        }?: pendingTransitionOnFinish
    }

    val categoriesRecursively: ArrayList<String> by lazy {
        superActivityClass?.let {
            ArrayList<String>(categories).apply { addAll(it.categoriesRecursively) }
        }?:categories
    }

    val flagsRecursively: ArrayList<Int> by lazy {
        superActivityClass?.let {
            ArrayList<Int>(flags).apply { addAll(it.flagsRecursively) }
        }?:flags
    }

    fun setupSuperClass(activityClasses: HashMap<Element, ActivityClass>) {
        val typeMirror = type.superclass
        if (typeMirror == null || typeMirror == Type.noType) {
            return
        }
        val superClassElement = (typeMirror as DeclaredType).asElement() as TypeElement
        this.superActivityClass = activityClasses[superClassElement]
        if (this.superActivityClass != null && this.activityResultClass != null) {
            this.activityResultClass!!.setSuperActivityResultClass(this.superActivityClass!!.activityResultClass)
        }
    }

    fun addSymbol(field: RequiredField) {
        requiredFields.add(field)
    }

    private fun buildConstants(typeBuilder: TypeSpec.Builder) {
        for (field in requiredFieldsRecursively) {
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    field.prefix + Utils.camelToUnderline(field.name),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", field.name)
                    .build())
        }
        if (activityResultClass != null) {
            for (resultEntity in activityResultClass!!.resultEntitiesRecursively) {
                typeBuilder.addField(FieldSpec.builder(String::class.java,
                        CONSTS_RESULT_PREFIX + Utils.camelToUnderline(resultEntity.name),
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", resultEntity.name)
                        .build())
            }
        }
    }

    private fun buildFinishMethod(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("finishWithTransition")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get(type), "activity")
                .addStatement("\$T.finishAfterTransition(activity)", JavaTypes.ACTIVITY_COMPAT)
        val pendingTransitionOnFinish = pendingTransitionOnFinishRecursively
        if (pendingTransitionOnFinish.exitAnim != PendingTransition.DEFAULT || pendingTransitionOnFinish.enterAnim != PendingTransition.DEFAULT) {
            methodBuilder.addStatement("activity.overridePendingTransition(\$L, \$L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }
        typeBuilder.addMethod(methodBuilder.build())
    }

    private fun buildFinishFuncKt(fileSpecBuilder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("finishWithTransition")
                .receiver(type.asType().asTypeName())
                .addStatement("%T.finishAfterTransition(this)", KotlinTypes.ACTIVITY_COMPAT)

        val pendingTransitionOnFinish = pendingTransitionOnFinishRecursively
        if (pendingTransitionOnFinish.exitAnim != PendingTransition.DEFAULT || pendingTransitionOnFinish.enterAnim != PendingTransition.DEFAULT) {
            funBuilder.addStatement("overridePendingTransition(%L, %L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }
        fileSpecBuilder.addFunction(funBuilder.build())
    }

    fun buildStartFunKt(fileSpecBuilder: FileSpec.Builder) {
        StartFunctionKt(this, EXT_FUN_NAME_PREFIX + simpleName).brew(fileSpecBuilder);
    }

    fun brew(filer: Filer) {
        if (type.modifiers.contains(Modifier.ABSTRACT)) return
        val typeBuilder = TypeSpec.classBuilder(simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        buildConstants(typeBuilder)

        InjectMethod(this).brew(typeBuilder)
        SaveStateMethod(this).brew(typeBuilder)

        activityResultClass?.buildOnActivityResultListenerInterface()?.let(typeBuilder::addType)

        when (generateMode) {
            GenerateMode.JavaOnly -> {
                MethodsBuilder.buildStartMethod(this, typeBuilder)
                buildFinishMethod(typeBuilder)

                activityResultClass?.buildFinishWithResultMethod()?.let(typeBuilder::addMethod)
            }
            GenerateMode.Both -> {
                MethodsBuilder.buildStartMethod(this, typeBuilder)
                buildFinishMethod(typeBuilder)

                activityResultClass?.buildFinishWithResultMethod()?.let(typeBuilder::addMethod)

                //region kotlin
                val fileSpecBuilder = FileSpec.builder(packageName, simpleName + POSIX)
                buildStartFunKt(fileSpecBuilder)
                buildFinishFuncKt(fileSpecBuilder)

                activityResultClass?.buildFinishWithResultKt()?.let(fileSpecBuilder::addFunction)

                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            GenerateMode.KotlinOnly -> {
                val fileSpecBuilder = FileSpec.builder(packageName, simpleName + POSIX)
                buildStartFunKt(fileSpecBuilder)
                buildFinishFuncKt(fileSpecBuilder)
                activityResultClass?.buildFinishWithResultKt()?.let(fileSpecBuilder::addFunction)
                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            Auto ->{
                //Won't happen.
            }
        }//endregion

        writeJavaToFile(filer, typeBuilder.build())
    }

    private fun writeJavaToFile(filer: Filer, typeSpec: TypeSpec) {
        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(filer)
    }

    private fun writeKotlinToFile(filer: Filer, fileSpec: FileSpec) {
        filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileSpec.name + ".kt")
                .openWriter()
                .also(fileSpec::writeTo)
                .close()
    }

    companion object {
        public const val METHOD_NAME = "start"
        public const val METHOD_NAME_NO_OPTIONAL = METHOD_NAME + "WithoutOptional"
        public const val METHOD_NAME_FOR_OPTIONAL = METHOD_NAME + "WithOptional"
        public const val EXT_FUN_NAME_PREFIX = METHOD_NAME
        public const val POSIX = "Builder"

        public const val CONSTS_RESULT_PREFIX = "RESULT_"

        public val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
    }
}
