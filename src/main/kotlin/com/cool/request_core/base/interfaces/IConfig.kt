package com.cool.request_core.base.interfaces

import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.cool.request_core.base.annotations.Configuration
import com.cool.request_core.base.bean.ControllerReferrer
import com.cool.request_core.base.bean.ControllerResult
import com.cool.request_core.base.bean.ResourceReferrer
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.RtClient

/**
 * 配置注册类，需要搭配ConfigRegister 注解同时使用
 * 方法返回true表示允许通过，返回false 表示拦截
 */
open class IConfig {
    open fun onCreate(){}

    open fun init(annotation: Configuration, clazz: Any){

    }


    open fun onRequestPre(request: Request, response: Response, controllerReferrer: ControllerReferrer):Boolean{
        return true
    }

    open fun onRequestEnd(request: Request, response: Response, controllerResult: ControllerResult) :Boolean{
        return true
    }

    open fun onResourceRequest(request: Request,response: Response,resourceReferrer: ResourceReferrer):Boolean{
        return true
    }


    //rt协议链接
    open fun onRtIn(client: RtClient,request: Request,response: Response):Boolean{
        return true
    }

    //rt协议消息进入
    open fun onRtMessage(client: RtClient,request: Request,response: Response):Boolean{
        return true
    }

    //rt协议断开链接
    open fun onRtOut(client: RtClient):Boolean{
        return true
    }
}