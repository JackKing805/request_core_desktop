package com.cool.request_core.base.bean

import java.lang.reflect.Method

data class ControllerReferrer(
    val path:String,
    val instance:Any,
    val method: Method,
)