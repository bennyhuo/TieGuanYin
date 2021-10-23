package com.bennyhuo.tieguanyin.compiler.ksp.activity

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.ResultParameter
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.asResultParameter
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByTypeOrNull
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isDefault
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.util.*

/**
 * Created by benny on 1/29/18.
 */

class ActivityClass
private constructor(type: KSClassDeclaration, builder: Builder): BasicClass(type, builder) {

    companion object {
        private val activityClassCache = WeakHashMap<KSClassDeclaration, ActivityClass>()

        fun getOrNull(declaration: KSClassDeclaration): ActivityClass? {
            var activityClass = activityClassCache[declaration]
            if (activityClass == null) {
                val builder = declaration.getFirstAnnotationByTypeOrNull(Builder::class) ?: return null
                activityClass = ActivityClass(declaration, builder)
                activityClassCache[declaration] = activityClass
            }
            return activityClass
        }

        fun create(typeElement: KSClassDeclaration): ActivityClass {
            return activityClassCache.getOrPut(typeElement){
                ActivityClass(typeElement, typeElement.getFirstAnnotationByType(Builder::class))
            }
        }
    }

    private val declaredPendingTransition: PendingTransition
    private val declaredPendingTransitionOnFinish: PendingTransition
    private val declaredCategories = ArrayList<String>()
    private val declaredFlags = ArrayList<Int>()
    private val declaredResultParameters = TreeSet<ResultParameter>()

    val builder = ActivityClassBuilder(this)

    init {
        declaredFlags.addAll(builder.flags.asList())
        declaredCategories.addAll(builder.categories)

        declaredPendingTransition = builder.pendingTransition
        declaredPendingTransitionOnFinish = builder.pendingTransitionOnFinish

        if (builder.resultTypes.isNotEmpty()) {
            builder.resultTypes.mapTo(declaredResultParameters, ResultEntity::asResultParameter)
        }
    }

    var pendingTransition = declaredPendingTransition
        private set

    var pendingTransitionOnFinish = declaredPendingTransitionOnFinish
        private set

    val categories = ArrayList(declaredCategories)

    val flags = ArrayList(declaredFlags)

    val resultParameters = TreeSet(declaredResultParameters)

    val hasResult: Boolean
        get() = resultParameters.isNotEmpty()

    init {
        val superActivityClass = superClass as? ActivityClass
        if (superActivityClass != null) {
            categories += superActivityClass.categories
            flags += superActivityClass.flags
            resultParameters.addAll(superActivityClass.resultParameters)

            if (pendingTransition.isDefault()) pendingTransition = superActivityClass.pendingTransition
            if (pendingTransitionOnFinish.isDefault()) pendingTransitionOnFinish = superActivityClass.pendingTransitionOnFinish
        }
    }

    override fun createSuperClass(superClassDeclaration: KSClassDeclaration) = getOrNull(superClassDeclaration)
}
