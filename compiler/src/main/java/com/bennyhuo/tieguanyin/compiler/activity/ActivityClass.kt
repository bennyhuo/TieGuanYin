package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.basic.entity.ResultParameter
import com.bennyhuo.tieguanyin.compiler.basic.entity.asResultParameter
import com.bennyhuo.tieguanyin.compiler.utils.isDefault
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 1/29/18.
 */

class ActivityClass(type: TypeElement): BasicClass(type) {

    private val declaredPendingTransition: PendingTransition
    private val declaredPendingTransitionOnFinish: PendingTransition
    private val declaredCategories = ArrayList<String>()
    private val declaredFlags = ArrayList<Int>()
    private val declaredResultParameters = TreeSet<ResultParameter>()

    private var superActivityClass: ActivityClass? = null

    val builder = ActivityClassBuilder(this)

    init {
        val generateBuilder = type.getAnnotation(Builder::class.java)
        declaredFlags.addAll(generateBuilder.flags.asList())
        declaredCategories.addAll(generateBuilder.categories)

        declaredPendingTransition = generateBuilder.pendingTransition
        declaredPendingTransitionOnFinish = generateBuilder.pendingTransitionOnFinish

        if (generateBuilder.resultTypes.isNotEmpty()) {
            generateBuilder.resultTypes.mapTo(declaredResultParameters, ResultEntity::asResultParameter)
        }
    }

    var pendingTransition = declaredPendingTransition
        private set

    var pendingTransitionOnFinish = declaredPendingTransitionOnFinish
        private set

    var categories: List<String> = ArrayList(declaredCategories)
        private set

    var flags: List<Int> = ArrayList(declaredFlags)
        private set

    var resultParameters: TreeSet<ResultParameter> = TreeSet(declaredResultParameters)
        private set

    val hasResult: Boolean
        get() = resultParameters.isNotEmpty()

    fun setUpSuperClass(activityClasses: HashMap<Element, ActivityClass>) {
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
