package com.cool.request_core.base.annotations


@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION,AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bean(
    val name:String=""//bean的名字，默认是类的type
)
