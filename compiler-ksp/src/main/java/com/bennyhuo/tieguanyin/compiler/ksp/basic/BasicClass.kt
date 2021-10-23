package com.bennyhuo.tieguanyin.compiler.ksp.basic

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByTypeOrNull
import com.bennyhuo.tieguanyin.compiler.ksp.utils.superType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import java.util.*
import kotlin.collections.ArrayList

abstract class BasicClass(val declaration: KSClassDeclaration, builder: Builder) {

    val simpleName = declaration.simpleName.asString()

    val builderClassName: String by lazy {
        val list = ArrayList<String>()
        list += simpleName
        var element = declaration.parentDeclaration
        while (element as? KSClassDeclaration != null){
            list += element.simpleName.asString()
            element = element.parentDeclaration
        }
        list.reversed().joinToString("_") + POSIX
    }
    val packageName: String = declaration.packageName.asString()

    private val declaredFields = TreeSet<Field>()
    private val declaredSharedElements = ArrayList<SharedElementEntity>()

    var superClass: BasicClass? = null
        private set

    val isAbstract = Modifier.ABSTRACT in declaration.modifiers

    init {
        builder.sharedElements.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        builder.sharedElementsByNames.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        builder.sharedElementsWithName.mapTo(declaredSharedElements) { SharedElementEntity(it) }
    }

    val sharedElements = ArrayList(declaredSharedElements)

    val fields: TreeSet<Field> = TreeSet()
    init {
        initFields()
        initSuperClass()
    }

    private fun initFields() {
        declaration.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .forEach {
                val optional = it.getFirstAnnotationByTypeOrNull(Optional::class)
                if (optional == null) {
                    val required = it.getFirstAnnotationByTypeOrNull(Required::class)
                    if (required != null) {
                        addField(Field(it))
                    }
                } else {
                    addField(OptionalField(it, optional))
                }
            }
    }

    private fun addField(field: Field) {
        declaredFields.add(field)
        fields.add(field)
    }

    private fun initSuperClass() {
        val superClassDeclaration = declaration.superType() ?: return
        val superClass = createSuperClass(superClassDeclaration)
        if (superClass != null) {
            fields.addAll(superClass.fields)
            sharedElements += superClass.sharedElements
            this.superClass = superClass
        }
    }

    abstract fun createSuperClass(superClassDeclaration: KSClassDeclaration): BasicClass?

    companion object {
        const val POSIX = "Builder"
    }
}