package com.cool.request_core.core.factory

import com.cool.request_core.base.bean.ControllerReferrer
import com.cool.request_core.base.bean.ControllerResult
import com.cool.request_core.base.bean.ResourceReferrer
import com.cool.request_core.core.delegator.RequestController
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    fun onRequestPre(
        request: Request,
        response: Response,
        controllerMapper: RequestController?
    ): Boolean {
        val referer = request.getPackage().getHeader().getReferer()
        val isResourcesRequest = request.isResourceRequest()
        if (!isResourcesRequest) {
            if (controllerMapper!=null){
                val controllerReferrer = ControllerReferrer(
                    controllerMapper.path,
                    controllerMapper.instance,
                    controllerMapper.method
                )
                InjectFactory.getConfigRegisters().forEach {
                    if (!it.instance.onRequestPre(request, response, controllerReferrer)) {
                        return false
                    }
                }
            }
        } else {
            val resourceReferrer = ResourceReferrer(referer, request.getResourcesPath()!!)
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onResourceRequest(request, response, resourceReferrer)) {
                    return false
                }
            }
        }
        return true
    }

    fun onRequestEnd(
        request: Request,
        response: Response,
        controllerMapper: RequestController,
        result: Any
    ): Boolean {
        InjectFactory.getConfigRegisters().forEach {
            val controllerReferrer = ControllerReferrer(
                controllerMapper.path,
                controllerMapper.instance,
                controllerMapper.method
            )
            val controllerResult = ControllerResult(controllerReferrer, result)
            if (!it.instance.onRequestEnd(request, response, controllerResult)) {
                return false
            }
        }
        return true
    }

    fun matchController(path: String): ControllerMapper? {
        return InjectFactory.getController(path)
    }
}


