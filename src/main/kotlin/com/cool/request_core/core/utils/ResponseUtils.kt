package com.cool.request_core.core.utils

import com.cool.request_core.GsonUtils
import com.cool.request_core.core.constants.FileType
import com.cool.request_core.core.exception.IllRedirectProtocolException
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtHeader
import java.io.File


internal object ResponseUtils{
    private data class Redirect(
        val path:String,
        val code:Int
    )

    fun dispatcherError(response: Response, errorCode: Int) {
        response.setResponseStatusCode(errorCode)
        val type = response.getHeader(RtHeader.CONTENT_TYPE.content)?:RtContentType.TEXT_HTML.content
        response.write(RtCode.match(errorCode).message, type)
    }

    fun dispatcherReturn(
        isRestController: Boolean,
        response: Response,
        returnObject: Any?
    ) {
        if (returnObject == null) {
            dispatcherError(response,  RtCode._500.code)
            return
        }
        if (returnObject is Unit) {
            dispatcherError(response,  RtCode._500.code)
        } else {
            if (isRestController) {
                val type = response.getHeader(RtHeader.CONTENT_TYPE.content)?:RtContentType.JSON.content
                if (returnObject is String) {
                    response.write(returnObject, type)
                } else {
                    response.write(GsonUtils.toJson(returnObject), type)
                }
            } else {
                if (returnObject is String) {
                    val fileType = FileType.matchFileType(returnObject)
                    if (fileType==null){
                        if (returnObject.startsWith("redirect:")){//重定向链接
                            val redirect = if (returnObject.startsWith("redirect-forever:")){//301
                                Redirect(
                                    returnObject.replace("redirect-forever:",""),
                                    RtCode._301.code
                                )
                            }else if (returnObject.startsWith("redirect-temporary:")){//307
                                Redirect(
                                    returnObject.replace("redirect-temporary:",""),
                                    RtCode._307.code
                                )
                            }else if (returnObject.startsWith("redirect:")){//302
                                Redirect(
                                    returnObject.replace("redirect:",""),
                                    RtCode._302.code
                                )
                            }else{
                                throw IllRedirectProtocolException(returnObject)
                            }

                            val location = if (
                                redirect.path.startsWith("https:") ||
                                redirect.path.startsWith("http:") ||
                                redirect.path.startsWith("ftp:") ||
                                redirect.path.startsWith("ws:")
                            ){
                                redirect.path
                            }else{
                                response.getPackage().getRootAbsolutePath() + if (redirect.path.startsWith("/")){
                                    redirect.path.substring(1)
                                }else{
                                    redirect.path
                                }
                            }
                            response.setResponseStatusCode(redirect.code)
                            response.setContentType(response.getPackage().getHeader().getContentType())
                            response.setHeader("Location",location)
                            response.sendHeader()
                        } else if(returnObject.startsWith("{")&& returnObject.endsWith("}")){
                            val type = response.getHeader(RtHeader.CONTENT_TYPE.content)?:RtContentType.JSON.content
                            response.write(returnObject, type)
                        }else {
                            val type = response.getHeader(RtHeader.CONTENT_TYPE.content)?:RtContentType.TEXT_PLAIN.content
                            response.write(returnObject, type)
                        }
                    }else{
                        when(fileType.fileType){
                            FileType.FILE -> {
                                response.writeFile(File(fileType.fileName))
                            }
                        }
                    }
                } else if (returnObject is File) {
                    response.writeFile(returnObject)
                } else {
                    val type = response.getHeader(RtHeader.CONTENT_TYPE.content)?: RtContentType.TEXT_PLAIN.content
                    response.write(GsonUtils.toJson(returnObject),type)
                }
            }
        }
    }
}
