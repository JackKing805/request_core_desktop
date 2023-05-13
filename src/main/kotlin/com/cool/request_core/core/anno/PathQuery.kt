package com.cool.request_core.core.anno

/**
 * /q/b/{id}
 * 根据名字获取path中的参数
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PathQuery(//查找 url里面的参数或者xxxww里面的参数
    val name: String
)
