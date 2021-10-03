package com.bennyhuo.tieguanyin.annotations

import kotlin.reflect.KClass

annotation class ResultEntity(val name: String, val type: KClass<*>)