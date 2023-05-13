package com.cool.request_core.core.exception

class PathParamsConvertErrorException(param:String,type:Class<*>):Exception("$param can't convert to $type")