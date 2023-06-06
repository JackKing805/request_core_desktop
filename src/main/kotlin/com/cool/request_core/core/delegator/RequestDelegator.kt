package com.cool.request_core.core.delegator

import com.cool.request_core.GsonUtils
import com.cool.request_core.base.bean.RequestMethod
import com.cool.request_core.core.Core
import com.cool.request_core.core.anno.ExceptionHandler
import com.cool.request_core.core.anno.ExceptionRule
import com.cool.request_core.core.anno.ParamsQuery
import com.cool.request_core.core.anno.PathQuery
import com.cool.request_core.core.bean.ParameterBean
import com.cool.request_core.core.bean.ParametersBeanCreator
import com.cool.request_core.core.exception.InvokeMethodException
import com.cool.request_core.core.extensions.pathParams
import com.cool.request_core.core.extensions.toBasicTargetType
import com.cool.request_core.core.extensions.toObject
import com.cool.request_core.core.extensions.toRequestController
import com.cool.request_core.core.factory.ControllerMapper
import com.cool.request_core.core.factory.ControllerPathParams
import com.cool.request_core.core.factory.InjectFactory
import com.cool.request_core.ReflectUtils
import com.cool.request_core.core.factory.RequestFactory
import com.cool.request_core.core.utils.ResponseUtils
import com.cool.request_core.core.utils.reflect.InjectUtils
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.request.model.MultipartFile
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * 请求分发
 */
internal object RequestDelegator {
    internal fun dispatcher(request: Request, response: Response) {
        val requestURI = request.getPackage().getRequestURI()
        val controllerMapper = RequestFactory.matchController(requestURI.path)?.toRequestController {
            when(it.type){
                Request::class.java->request
                Response::class.java->response
                ProtocolPackage::class.java->request.getPackage()
                ISession::class.java->request.getPackage().getSession()
                RtContext::class.java->request.getContext()
                ProtocolPackage.Header::class.java->request.getPackage().getHeader()
                else->null
            }
        }
        Core.getIRequestListener()?.onRequest(requestURI.path ?: "")

        try {
            if (!RequestFactory.onRequestPre(request, response, controllerMapper)) {
                return
            }
        } catch (e: Exception) {
            onException(response, e)
            return
        }


        if (controllerMapper != null) {
            if (controllerMapper.requestMethod.content.equals(request.getPackage().getRequestMethod(), true)) {
                try {
                    val multipartFormData = request.getMultipartFormData()
                    val pbBean = ParametersBeanCreator(request).create()
                    multipartFormData?.getParameters()?.let {
                        pbBean.add(it)
                    }
                    val body = request.getByteBody()

                    val invoke = try {
                        InjectUtils.invokeMethod(
                            controllerMapper.instance,
                            controllerMapper.method
                        ) {
                            val pathQuery = ReflectUtils.getAnnotation(it, PathQuery::class.java)
                            if (pathQuery!=null) {
                                if (controllerMapper.pathParam==null){
                                    throw NullPointerException("this ${request.getPackage().getRelativePath()} not support path request")
                                }else{
                                    val pathParams = controllerMapper.pathParams(requestURI.path)
                                    if (pathParams!=null && controllerMapper.pathParam.name == pathQuery.name){
                                        pathParams.toBasicTargetType(it.type)
                                    }else{
                                        null
                                    }
                                }
                            }else {
                                val paramQuery = ReflectUtils.getAnnotation(it, ParamsQuery::class.java)
                                (if (paramQuery != null) {
                                    pbBean.find(paramQuery.name, it.type)
                                } else {
                                    null
                                }) ?: when (it.parameterizedType) {
                                        RtContext::class.java -> {
                                            request.getContext()
                                        }
                                        Request::class.java -> {
                                            request
                                        }
                                        Response::class.java -> {
                                            response
                                        }
                                        ParameterBean::class.java -> {
                                            pbBean
                                        }
                                        GsonUtils.getListType(MultipartFile::class.java) -> multipartFormData?.getFiles()?.map { it.value }
                                        GsonUtils.getMapType(String::class.java, MultipartFile::class.java) -> multipartFormData?.getFiles()?.map { it.value }
                                        MultipartFile::class.java -> {
                                            if (paramQuery != null) {
                                                multipartFormData?.getFile(paramQuery.name)
                                            } else {
                                                multipartFormData?.getFiles()?.map { it.value }
                                                    ?.first()
                                            }
                                        }
                                        else -> {
                                            body?.toObject(it.type)
                                        }
                                    }
                            }
                        }
                    } catch (e: InvokeMethodException) {
                        e.printStackTrace()
                        ResponseUtils.dispatcherError(response,  RtCode._500.code)
                        return
                    } catch (e: Exception) {
                        onException(response, e)
                        return
                    }
                    if (invoke == null) {
                        ResponseUtils.dispatcherError(response, RtCode._500.code)
                    } else {
                        if (RequestFactory.onRequestEnd(request, response,controllerMapper,invoke)) {
                            ResponseUtils.dispatcherReturn(
                                controllerMapper.isRestController,
                                response,
                                invoke
                            )
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    ResponseUtils.dispatcherError(response, RtCode._502.code)
                }
                return
            } else {
                ResponseUtils.dispatcherError(response, RtCode._405.code)
                return
            }
        }
        ResponseUtils.dispatcherError(response, RtCode._404.code)
    }


    internal fun onRtIn(client: RtClient,request: Request, response: Response) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtIn(client,request,response)){
                    return
                }
            }
        } catch (e: Exception) {
            onException(response, e)
        }
    }

    internal fun onRtMessage(client:RtClient,request: Request, response: Response) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtMessage(client,request,response)){
                    return
                }
            }
        } catch (e: Exception) {
            onException(response, e)
        }
    }

    internal fun onRtOut(client: RtClient) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtOut(client)){
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun onException(response: Response, e: Exception) {
        val realException = if (e is InvocationTargetException) {
            e.targetException
        } else {
            e
        }

        if (requestExceptionHandler == null) {
            val annotationBean = Core.getAnnotationBean(ExceptionRule::class.java)
            if (annotationBean != null) {
                requestExceptionHandler = RequestExceptionHandler(annotationBean)
            }
        }


        if (requestExceptionHandler != null) {
            if (requestExceptionHandler!!.dealException(response, realException)) {
                return
            }
        }
        ResponseUtils.dispatcherError(response, 500)
    }

    private var requestExceptionHandler: RequestExceptionHandler? = null


    private class RequestExceptionHandler(private val ins: Any) {
        private class ExceptionMethod(
            val method: Method,
            val exceptionClass: KClass<out Throwable>
        )

        private val exceptionClasses = mutableListOf<ExceptionMethod>()

        init {
            val annotationBean = Core.getAnnotationBean(ExceptionRule::class.java)
            if (annotationBean != null) {
                annotationBean::class.java.declaredMethods.forEach { m ->
                    val handlerAnno = ReflectUtils.getAnnotation(m, ExceptionHandler::class.java)
                    if (handlerAnno != null) {
                        exceptionClasses.add(ExceptionMethod(m, handlerAnno.exceptionClasses))
                    }
                }
            }
        }


        fun dealException(response: Response, e: Throwable): Boolean {
            e.printStackTrace()
            val AllExce = exceptionClasses.find { it.exceptionClass == Exception::class }
            if (AllExce != null) {
                val invokeMethod = try {
                    InjectUtils.invokeMethod(ins, AllExce.method, provider = arrayOf(e))
                } catch (e: Exception) {
                    return false
                }
                if (invokeMethod != null) {
                    ResponseUtils.dispatcherReturn(true, response, invokeMethod)
                    return true
                }
            } else {
                val exce = exceptionClasses.find { it.exceptionClass == e::class }
                if (exce != null) {
                    val invokeMethod = try {
                        InjectUtils.invokeMethod(ins, exce.method, provider = arrayOf(e))
                    } catch (e: Exception) {
                        return false
                    }
                    if (invokeMethod != null) {
                        ResponseUtils.dispatcherReturn(true, response, invokeMethod)
                        return true
                    }
                }
            }
            return false
        }
    }
}

internal class RequestController(
    val instance:Any,
    val controllerMapper: ControllerMapper
){
    val clazz: Class<*> = controllerMapper.clazz
    val method: Method = controllerMapper.method
    val requestMethod: RequestMethod = controllerMapper.requestMethod
    val isRestController: Boolean = controllerMapper.isRestController
    val path: String = controllerMapper.path
    val pathParam: ControllerPathParams?=controllerMapper.pathParam
}
