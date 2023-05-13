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


    fun init(more:MutableList<Class<*>>){
        inject(more)
    }

    fun inject(more:MutableList<Class<*>>){
        InjectFactory.inject(more)
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