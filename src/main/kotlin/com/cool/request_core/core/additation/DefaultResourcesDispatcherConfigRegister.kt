package com.cool.request_core.core.additation

import com.cool.request_core.base.annotations.ConfigRegister
import com.cool.request_core.base.annotations.Configuration
import com.cool.request_core.base.bean.ResourceReferrer
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.constants.FileType
import com.cool.request_core.core.utils.reflect.ReflectUtils
import com.cool.request_core.core.Core
import com.cool.request_core.core.extensions.isFileExists
import com.cool.request_core.core.utils.ResponseUtils
import com.cool.request_core.core.utils.reflect.InjectUtils
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(-1, registerClass = Any::class)
class DefaultResourcesDispatcherConfigRegister : IConfig() {
    private  val resourcesDispatchers: MutableList<ResourcesDeal> = mutableListOf()
    override fun init(annotation: Configuration, clazz:Any) {
        clazz::class.java.methods.forEach {
            val parameters = it.parameters
            if (parameters.size==1 && ReflectUtils.isSameClass(ResourcesDeal::class.java,parameters[0].type)){
                val resourcesDispatcher = ResourcesDeal()
                InjectUtils.invokeMethod(clazz,it, arrayOf(resourcesDispatcher))
                if (!resourcesDispatcher.isBuild()){
                    throw IllegalStateException("please add resources handler")
                }
                resourcesDispatchers.add(resourcesDispatcher)
            }
        }
    }

    override fun onCreate() {
        val bean = Core.getBean(ResourcesDeal::class.java)
        if (bean!=null){
            val ss = bean as ResourcesDeal
            if (!resourcesDispatchers.contains(ss)){
                resourcesDispatchers.add(bean)
            }
        }
    }


    override fun onResourceRequest(
        request: Request,
        response: Response,
        resourceReferrer: ResourceReferrer
    ): Boolean {
        if (resourcesDispatchers.isNotEmpty()){
            for (i in resourcesDispatchers){
                if (i.dealResources(request, response,resourceReferrer.resourcesPath)){
                    return false
                }
            }
        }
        dealDefault(response,resourceReferrer.resourcesPath)
        return true
    }
    private fun dealDefault(response: Response,resourcesPath:String){
        if (!resourcesPath.isFileExists()){
            return
        }
        ResponseUtils.dispatcherReturn(false,response,FileType.FILE.content+resourcesPath)
    }

    class ResourcesDeal(
        private var url:String = "/"
    ){
        private var resourcesDispatcher: ResourcesDispatcher?=null

        internal fun isBuild() = resourcesDispatcher != null

        fun interceptor(url:String): ResourcesDeal {
            this.url = url
            return this
        }

        fun build(requestHandler: ResourcesDispatcher){
            this.resourcesDispatcher = requestHandler
        }

        internal fun dealResources(request: Request,response: Response,resourcesPath: String):Boolean{
            val requestURI = request.getPackage().getRequestURI()
            val path = requestURI.path?:""
            return if (path == url || path.startsWith(url)){
                val resultResourcesPath = resourcesDispatcher!!.onResourcesRequest(request, response,resourcesPath)
                ResponseUtils.dispatcherReturn(false,response,resultResourcesPath)
                true
            }else{
                false
            }
        }
    }

    interface ResourcesDispatcher{
        fun onResourcesRequest(request: Request,response: Response,resourcesPath:String):String
    }
}
