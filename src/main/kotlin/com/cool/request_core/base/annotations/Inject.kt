package com.cool.request_core.base.annotations

/**
 * 从bean中注入
 */
@Target(AnnotationTarget.FIELD,AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(
    val name:String=""
)
