package com.cool.request_core.core.additation

import com.cool.request_core.base.annotations.ConfigRegister
import com.cool.request_core.base.annotations.Configuration
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.factory.InjectFactory
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(priority = -1)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient: RtClient?=null

    override fun init(annotation: Configuration, clazz: Any) {
        val bean2 = InjectFactory.getBean(RtClient::class.java)
        if (bean2!=null){
            rtClient = bean2 as RtClient
        }
    }

    override fun onRtIn(client: Client,request: Request, response: Response): Boolean {
        rtClient?.onRtIn(client,request,response)
        return false
    }

    override fun onRtMessage(request: Request, response: Response): Boolean {
        rtClient?.onRtMessage(request,response)
        return false
    }
    override fun onRtOut(client: Client): Boolean {
        rtClient?.onRtOut(client)
        return false
    }

    interface RtClient{
        fun handUrl():String

        fun onRtIn(client: Client,request: Request,response: Response)

        fun onRtMessage(request: Request,response: Response)
        fun onRtOut(client: Client)
    }
}