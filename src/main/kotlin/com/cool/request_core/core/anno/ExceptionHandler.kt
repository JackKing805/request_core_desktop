package com.cool.request_core.core.anno

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExceptionHandler(
    val exceptionClasses: KClass<out Throwable>
)
