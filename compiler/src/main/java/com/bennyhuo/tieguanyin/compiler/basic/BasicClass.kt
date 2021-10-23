package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.aptutils.types.packageName
import com.bennyhuo.aptutils.types.simpleName
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.entity.SharedElementEntity
import java.util.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import kotlin.collections.ArrayList

abstract class BasicClass(val typeElement: TypeElement, builder: Builder) {

    val simpleName = typeElement.simpleName()

    val builderClassName: String by lazy {
        val list = ArrayList<String>()
        list += typeElement.simpleName()
        var element = typeElement.enclosingElement
        while (element != null && element.kind != ElementKind.PACKAGE){
            list += element.simpleName()
            element = element.enclosingElement
        }
        list.reversed().joinToString("_") + POSIX
    }
    val packageName: String = typeElement.packageName()

    private val declaredFields = TreeSet<Field>()
    private val declaredSharedElements = ArrayList<SharedElementEntity>()

    val generateMode: GenerateMode

    var superClass: BasicClass? = null
        private set

    val isAbstract = typeElement.modifiers.contains(Modifier.ABSTRACT)

    init {
        generateMode = if (builder.mode == GenerateMode.Auto) {
            //如果有这个注解，说明就是 Kotlin 类
            val isKotlin = typeElement.getAnnotation(META_DATA) != null
            if (isKotlin) GenerateMode.Both else GenerateMode.JavaOnly
        } else builder.mode

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
        typeElement.enclosedElements
            .filterIsInstance<VariableElement>()
            .forEach {
                val optional = it.getAnnotation(Optional::class.java)
                if (optional == null) {
                    val required = it.getAnnotation(Required::class.java)
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
        val typeMirror = typeElement.superclass ?: return
        if (typeMirror.kind == TypeKind.NONE) return

        val superClassElement = (typeMirror as DeclaredType).asElement() as TypeElement
        val superClass = createSuperClass(superClassElement)
        if (superClass != null) {
            fields.addAll(superClass.fields)
            sharedElements += superClass.sharedElements
            this.superClass = superClass
        }
    }

    abstract fun createSuperClass(superClassElement: TypeElement): BasicClass?

    companion object {
        @Suppress("UNCHECKED_CAST")
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
        const val POSIX = "Builder"
    }
}