package com.cool.request_core.core.anno

/**
 * 根据名字获取url和body中的参数
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamsQuery(//查找 url里面的参数或者xxxww里面的参数
    val name: String
)
