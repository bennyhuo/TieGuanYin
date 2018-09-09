package com.bennyhuo.tieguanyin.compiler.shared

import com.bennyhuo.tieguanyin.annotations.SharedElement
import com.bennyhuo.tieguanyin.annotations.SharedElementByNames
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName

/**
 * Created by benny on 2/13/18.
 */

class SharedElementEntity(val targetName: String,
                          val sourceId: Int = 0,
                          val sourceName: String? = null) {

    constructor(sharedElement: SharedElement) : this(sharedElement.targetName, sourceId = sharedElement.sourceId)
    constructor(sharedElement: SharedElementWithName) : this(sharedElement.value, sourceName = sharedElement.value)
    constructor(sharedElement: SharedElementByNames) : this(sharedElement.target, sourceName = sharedElement.source)

}
