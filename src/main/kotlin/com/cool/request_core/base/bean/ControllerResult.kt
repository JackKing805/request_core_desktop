package com.cool.request_core.base.bean



data class ControllerResult(
    val controllerReferrer: ControllerReferrer,
    val controllerResult:Any
)