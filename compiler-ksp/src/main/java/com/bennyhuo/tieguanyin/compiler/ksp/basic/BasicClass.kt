package com.bennyhuo.tieguanyin.compiler.ksp.basic

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.superType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

abstract class BasicClass(val typeElement: KSClassDeclaration) {

    val simpleName = typeElement.simpleName.asString()

    val builderClassName: String by lazy {
        val list = ArrayList<String>()
        list += simpleName
        var element = typeElement.parentDeclaration
        while (element as? KSClassDeclaration != null){
            list += element.simpleName.asString()
            element = element.parentDeclaration
        }
        list.reversed().joinToString("_") + POSIX
    }
    val packageName: String = typeElement.packageName.asString()

    private val declaredFields = TreeSet<Field>()
    private val declaredSharedElements = ArrayList<SharedElementEntity>()

    val generateMode: GenerateMode

    private var superClass: BasicClass? = null

    val isAbstract = Modifier.ABSTRACT in typeElement.modifiers

    init {
        val generateBuilder = typeElement.getFirstAnnotationByType(Builder::class)
        generateMode = GenerateMode.KotlinOnly

        generateBuilder.sharedElements.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        generateBuilder.sharedElementsByNames.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        generateBuilder.sharedElementsWithName.mapTo(declaredSharedElements) { SharedElementEntity(it) }
    }

    val fields: TreeSet<Field> = TreeSet()

    val sharedElements = ArrayList(declaredSharedElements)

    fun <T: BasicClass> setUpSuperClass(classes: Map<KSNode, T>): T?{
        val superTypeElement = typeElement.superType()?: return null
        return classes[superTypeElement].also { superClass ->
            superClass?.also {
                fields.addAll(it.fields)
                sharedElements += it.sharedElements
            }
            this.superClass = superClass
        }
    }

    fun addSymbol(field: Field) {
        declaredFields.add(field)
        fields.add(field)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val META_DATA = Class.forName("kotlin.Metadata").kotlin as KClass<Annotation>
        const val POSIX = "Builder"
    }
}