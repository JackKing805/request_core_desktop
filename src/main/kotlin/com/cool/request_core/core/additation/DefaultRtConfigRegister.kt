package com.cool.request_core.core.additation

import com.cool.request_core.base.annotations.ConfigRegister
import com.cool.request_core.base.annotations.Configuration
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.factory.InjectFactory
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient

@ConfigRegister(priority = -1)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient: RtClientHandler?=null

    override fun init(annotation: Configuration, clazz: Any) {
        val bean2 = InjectFactory.getBean(RtClientHandler::class.java)
        if (bean2!=null){
            rtClient = bean2 as RtClientHandler
        }
    }

    override fun onRtIn(client: RtClient,request: Request, response: Response): Boolean {
        rtClient?.onRtIn(client,request,response)
        return false
    }

    override fun onRtMessage(client: RtClient,request: Request, response: Response): Boolean {
        rtClient?.onRtMessage(client,request,response)
        return false
    }
    override fun onRtOut(client: RtClient): Boolean {
        rtClient?.onRtOut(client)
        return false
    }

    interface RtClientHandler{
        fun handUrl():String

        fun onRtIn(client: RtClient,request: Request,response: Response)

        fun onRtMessage(client: RtClient,request: Request,response: Response)
        fun onRtOut(client: RtClient)
    }
}