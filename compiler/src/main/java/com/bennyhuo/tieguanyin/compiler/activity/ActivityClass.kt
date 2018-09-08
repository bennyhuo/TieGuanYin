package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField
import com.bennyhuo.tieguanyin.compiler.extensions.isDefault
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import com.sun.tools.javac.code.Type
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

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
    var generateMode: GenerateMode

    val builder = ActivityClassBuilder(this)

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

    val sharedElementsRecursively: ArrayList<SharedElementEntity> by lazy {
        superActivityClass?.let {
            ArrayList(sharedElements).apply {
                addAll(it.sharedElementsRecursively)
            }
        } ?: sharedElements
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
        val typeMirror = type.superclass?: return
        if (typeMirror == Type.noType) {
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

    companion object {
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
    }
}
