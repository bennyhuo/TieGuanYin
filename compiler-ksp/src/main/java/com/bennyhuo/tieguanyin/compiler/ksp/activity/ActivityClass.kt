package com.bennyhuo.tieguanyin.compiler.ksp.activity

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.ResultParameter
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.asResultParameter
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isDefault
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import java.util.*

/**
 * Created by benny on 1/29/18.
 */

class ActivityClass(type: KSClassDeclaration): BasicClass(type) {

    private val declaredPendingTransition: PendingTransition
    private val declaredPendingTransitionOnFinish: PendingTransition
    private val declaredCategories = ArrayList<String>()
    private val declaredFlags = ArrayList<Int>()
    private val declaredResultParameters = TreeSet<ResultParameter>()

    private var superActivityClass: ActivityClass? = null

    val builder = ActivityClassBuilder(this)

    init {
        val generateBuilder = type.getFirstAnnotationByType(Builder::class)
        declaredFlags.addAll(generateBuilder.flags.asList())
        declaredCategories.addAll(generateBuilder.categories)

        declaredPendingTransition = generateBuilder.pendingTransition
        declaredPendingTransitionOnFinish = generateBuilder.pendingTransitionOnFinish

        logger.warn("generateBuilder: ${generateBuilder.javaClass}")

        if (generateBuilder.resultTypes.isNotEmpty()) {
            generateBuilder.resultTypes.mapTo(declaredResultParameters, ResultEntity::asResultParameter)
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

    fun setUpSuperClass(activityClasses: HashMap<KSNode, ActivityClass>) {
        this.superActivityClass = super.setUpSuperClass(activityClasses)
        this.superActivityClass?.let { superActivityClass ->
            categories += superActivityClass.categories
            flags += superActivityClass.flags
            resultParameters.addAll(superActivityClass.resultParameters)

            if (pendingTransition.isDefault()) pendingTransition = superActivityClass.pendingTransition
            if (pendingTransitionOnFinish.isDefault()) pendingTransitionOnFinish = superActivityClass.pendingTransitionOnFinish
        }
    }
}
