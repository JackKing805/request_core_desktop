package com.cool.request_core.core.utils.reflect

import com.cool.request_core.base.annotations.Inject
import com.cool.request_core.core.exception.InvokeMethodException
import com.cool.request_core.core.factory.InjectFactory
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

object InjectUtils {
    @Throws(Exception::class)
    fun invokeMethod(any: Any,method: Method,provider:Array<Any> = arrayOf()):Any?{
        val args = mutableListOf<Any?>()
        val parameters = method.parameters
        parameters.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it, Inject::class.java)
            val injectBean = if (haveInject){
                getInjectBean(
                    it,
                    it.type
                )
            }else{
                provider.find { a-> ReflectUtils.isSameClass(it.type,a::class.java) }
            }
            args.add(injectBean)
        }
        return try {
            method.invoke(any,*args.toTypedArray())
        }catch (e:Exception){
            throw e
        }
    }


    @Throws(Exception::class)
    fun invokeMethod(any: Any,method: Method,provider:(pa:Parameter)->Any?):Any?{
        val args = mutableListOf<Any?>()
        val parameters = method.parameters
        parameters.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it,Inject::class.java)
            val injectBean = if (haveInject){
                getInjectBean(
                    it,
                    it.type
                )
            }else{
                val provider1 = provider(it)
                if (provider1==null&&it.type.componentType!=null){
                    throw NullPointerException("yuu can set$it to nullable")
                }
                provider1
            }
            args.add(injectBean)
        }
        return try {
            method.invoke(any,*args.toTypedArray())
        }catch (e:Exception){
            throw e
        }
    }


    @Throws(Exception::class)
    fun invokeInstance(clazz: Class<*>,provider:Array<Any> = arrayOf()):Any{
        val newInstance = clazz.newInstance()
        val fields = clazz.declaredFields
        fields.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it,Inject::class.java)
            if (haveInject){
                var injectBean = provider.find { a-> ReflectUtils.isSameClass(it.type,a::class.java) }
                if (injectBean==null){
                    injectBean = getInjectBean(
                        it,
                        it.type
                    )
                }

                it.isAccessible = true
                it.set(newInstance,injectBean)
            }
        }
        return newInstance
    }


    @Throws(Exception::class)
    fun invokeInstance(clazz: Class<*>,provider:(pa: Field)->Any?):Any{
        val newInstance = clazz.newInstance()
        val fields = clazz.declaredFields
        fields.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it,Inject::class.java)
            if (haveInject){
                var injectBean = provider(it)
                if (injectBean==null){
                    injectBean = getInjectBean(
                        it,
                        it.type
                    )
                }

                it.isAccessible = true
                it.set(newInstance,injectBean)
            }
        }
        return newInstance
    }

    private fun getInjectBean(any: AnnotatedElement, clazz: Class<*>):Any{
        val inject = ReflectUtils.haveAnnotation(any,Inject::class.java)
        if (!inject){
            throw InvokeMethodException("please use inject annotation to find bean in bean factory")
        }

        return InjectFactory.getBeanByInjectOrClass(
            any,
            clazz
        )?:throw InvokeMethodException("please provider bean:${clazz}")
    }
}
