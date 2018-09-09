package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.utils.isDefault
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass
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

    private var superActivityClass: ActivityClass? = null
    var activityResultClass: ActivityResultClass? = null

    val builder = ActivityClassBuilder(this)

    init {
        val generateBuilder = type.getAnnotation(Builder::class.java)
        declaredFlags.addAll(generateBuilder.flags.asList())
        declaredCategories.addAll(generateBuilder.categories)

        declaredPendingTransition = generateBuilder.pendingTransition
        declaredPendingTransitionOnFinish = generateBuilder.pendingTransitionOnFinish

        if (generateBuilder.resultTypes.isNotEmpty()) {
            activityResultClass = ActivityResultClass(this, generateBuilder.resultTypes)
        }
    }

    var pendingTransition = declaredPendingTransition
        private set

    var pendingTransitionOnFinish = declaredPendingTransitionOnFinish
        private set

    var categories: List<String> = declaredCategories
        private set

    var flags: List<Int> = declaredFlags
        private set

    fun setUpSuperClass(activityClasses: HashMap<Element, ActivityClass>) {
        this.superActivityClass = super.setUpSuperClass(activityClasses)
        this.activityResultClass?.superActivityResultClass = this.superActivityClass?.activityResultClass
        this.superActivityClass?.let { superActivityClass ->
            categories += superActivityClass.categories
            flags += superActivityClass.flags
            if (pendingTransition.isDefault()) pendingTransition = superActivityClass.pendingTransition
            if (pendingTransitionOnFinish.isDefault()) pendingTransitionOnFinish = superActivityClass.pendingTransitionOnFinish
        }
    }
}
