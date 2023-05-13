package com.cool.request_core.core.interfaces

import com.cool.request_core.core.constants.Status

interface IRequestListener {
    fun onStatusChange(status: Status)

    fun onRequest(url:String){}
}