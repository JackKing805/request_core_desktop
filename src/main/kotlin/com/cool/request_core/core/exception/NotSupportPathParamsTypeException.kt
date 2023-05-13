package com.cool.request_core.core.exception

class NotSupportPathParamsTypeException(type:Class<*>):Exception("$type is ill,path params not support")