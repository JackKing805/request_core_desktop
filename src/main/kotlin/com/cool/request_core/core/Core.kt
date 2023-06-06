package com.cool.request_core.core

import com.cool.request_core.core.factory.InjectFactory
import com.cool.request_core.core.interfaces.IRequestListener
import com.cool.request_core.core.service.RtCoreService
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.bean.RtFileConfig
import java.io.File

object Core {
    private var iRequestListener: IRequestListener?=null
    private var rtConfig: RtConfig = RtConfig(
        rtFileConfig = RtFileConfig(
            tempFileDir = "",
            saveFileDir = ""
        )
    )

    //先保存，调用init的时再初始化
    private val injects = mutableListOf<Class<*>>()
    private var isInit = false


    fun init(more:MutableList<Class<*>>){
        if (isInit){
            return
        }
        isInit = true
        more.addAll(injects)
        injects.clear()
        InjectFactory.inject(more)
    }

    fun inject(more:MutableList<Class<*>>){
        injects.addAll(more)
    }

    fun setRtConfig(rtConfig: RtConfig){
        Core.rtConfig = rtConfig
    }



    fun startServer(){
        RtCoreService.startRtCore()
    }

    fun stopServer(){
        RtCoreService.stopRtCore()
    }

    fun listen(iRequestListener: IRequestListener){
        Core.iRequestListener = iRequestListener
    }

    fun getRtConfig() = rtConfig


    internal fun getIRequestListener() = iRequestListener



    fun getBean(clazz: Class<*>) = InjectFactory.getBean(clazz)

    fun getBean(beanName: String) = InjectFactory.getBean(beanName)

    fun <T : Annotation> getAnnotationBean(annotationClass:Class<T>) = InjectFactory.getAnnotationBean(annotationClass)
}