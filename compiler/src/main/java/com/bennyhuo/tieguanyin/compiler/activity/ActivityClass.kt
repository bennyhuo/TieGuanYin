package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.extensions.isDefault
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 1/29/18.
 */

class ActivityClass(type: TypeElement): BasicClass(type) {

    private val pendingTransition: PendingTransition
    private val pendingTransitionOnFinish: PendingTransition
    private val categories = ArrayList<String>()
    private val flags = ArrayList<Int>()

    private var superActivityClass: ActivityClass? = null
    var activityResultClass: ActivityResultClass? = null

    val builder = ActivityClassBuilder(this)

    init {
        val generateBuilder = type.getAnnotation(Builder::class.java)
        flags.addAll(generateBuilder.flags.asList())
        categories.addAll(generateBuilder.categories)

        pendingTransition = generateBuilder.pendingTransition
        pendingTransitionOnFinish = generateBuilder.pendingTransitionOnFinish

        if (generateBuilder.resultTypes.isNotEmpty()) {
            activityResultClass = ActivityResultClass(this, generateBuilder.resultTypes)
        }
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

    fun setUpSuperClass(activityClasses: HashMap<Element, ActivityClass>) {
        this.superActivityClass = super.setUpSuperClass(activityClasses)
        if (this.superActivityClass != null && this.activityResultClass != null) {
            this.activityResultClass!!.setSuperActivityResultClass(this.superActivityClass!!.activityResultClass)
        }
    }
}
