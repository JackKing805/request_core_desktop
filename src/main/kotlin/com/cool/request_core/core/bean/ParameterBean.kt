package com.cool.request_core.core.bean

import com.cool.request_core.core.extensions.parameterToArray
import com.cool.request_core.core.extensions.toBasicTargetType
import com.jerry.rt.core.http.pojo.Request

//将url中的参数转化喂bean
data class ParameterBean(
    val parameters:MutableMap<String,String>
){

    fun add(p:Map<String,String>){
        parameters.putAll(p)
    }
    fun find(id:String,clazz: Class<*>):Any?{
        val s = parameters[id]
        if (s!=null){
            return try {
                s.toBasicTargetType(clazz)
            }catch (e:Exception){
                null
            }
        }
        return null
    }
}

class ParametersBeanCreator(private val request:Request){
    fun create(): ParameterBean {
        val map = mutableMapOf<String,String>()
        val pathParameters = request.getPackage().getRequestURI().parameterToArray(request.getCharset())
        map.putAll(pathParameters)
        val contentType = request.getPackage().getHeader().getContentType()
        if (contentType=="application/x-www-form-urlencoded"){
            val body = request.getBody()
            if (body!=null){
                val bodyParameters = body.parameterToArray(request.getCharset())
                map.putAll(bodyParameters)
            }
        }
        return ParameterBean(map)
    }
}
