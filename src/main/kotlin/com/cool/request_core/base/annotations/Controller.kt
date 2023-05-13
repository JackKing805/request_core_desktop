package com.cool.request_core.base.annotations

import com.cool.request_core.base.bean.RequestMethod

@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Controller(
    val value:String="",
    val requestMethod: RequestMethod = RequestMethod.GET,
    val isRest:Boolean = false
)
