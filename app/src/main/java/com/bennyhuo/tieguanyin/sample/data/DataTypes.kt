package com.bennyhuo.tieguanyin.sample.data

import com.bennyhuo.tieguanyin.annotations.Data
import com.bennyhuo.tieguanyin.annotations.Serialize

@Data
data class Book(
        @Serialize val id: Int,
        @Serialize(name = "person") val author: Person)

@Data
data class Person(
        @Serialize(name = "_id") val id: Int,
        @Serialize val name: String)