package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import com.sun.tools.javac.code.Type
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

abstract class BasicClass(val type: TypeElement) {

    val simpleName: String = TypeUtils.simpleName(type.asType())
    val packageName: String = TypeUtils.getPackageName(type)

    private val requiredFields = TreeSet<RequiredField>()
    private val sharedElements = ArrayList<SharedElementEntity>()

    val generateMode: GenerateMode

    private var superClass: BasicClass? = null

    val isAbstract = type.modifiers.contains(Modifier.ABSTRACT)

    init {
        val generateBuilder = type.getAnnotation(Builder::class.java)
        generateMode = if (generateBuilder.mode == GenerateMode.Auto) {
            //如果有这个注解，说明就是 Kotlin 类
            val isKotlin = type.getAnnotation(META_DATA) != null
            if (isKotlin) GenerateMode.Both else GenerateMode.JavaOnly
        } else generateBuilder.mode

        generateBuilder.sharedElements.mapTo(sharedElements){ SharedElementEntity(it) }
        generateBuilder.sharedElementsByNames.mapTo(sharedElements){ SharedElementEntity(it) }
        generateBuilder.sharedElementsWithName.mapTo(sharedElements){ SharedElementEntity(it) }
    }

    val requiredFieldsRecursively: TreeSet<RequiredField> by lazy {
        superClass?.let {
            TreeSet<RequiredField>(requiredFields)
                    .apply {
                        addAll(it.requiredFieldsRecursively)
                    }
        } ?: requiredFields
    }

    val sharedElementsRecursively: ArrayList<SharedElementEntity> by lazy {
        superClass?.let {
            ArrayList(sharedElements).apply {
                addAll(it.sharedElementsRecursively)
            }
        } ?: sharedElements
    }

    fun <T: BasicClass> setUpSuperClass(classes: Map<Element, T>): T?{
        val typeMirror = type.superclass?: return null
        if (typeMirror == Type.noType) return null
        val superClassElement = (typeMirror as DeclaredType).asElement() as TypeElement
        return classes[superClassElement].also { this.superClass = it }
    }

    fun addSymbol(field: RequiredField) {
        requiredFields.add(field)
    }

    companion object {
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
    }
}