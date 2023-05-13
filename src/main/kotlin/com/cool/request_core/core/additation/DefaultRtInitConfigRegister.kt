package com.cool.request_core.core.additation

import com.cool.request_core.base.annotations.ConfigRegister
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.Core
import com.cool.request_core.core.factory.InjectFactory
import com.jerry.rt.bean.*

@ConfigRegister(registerClass = Any::class)
class DefaultRtInitConfigRegister : IConfig() {


    override fun onCreate() {
        val bean1 = InjectFactory.getBean(RtConfig::class.java)
        if (bean1!=null){
            Core.setRtConfig(bean1 as RtConfig)
        }

        val bean2 = InjectFactory.getBean(RtSessionConfig::class.java)
        if (bean2!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtSessionConfig = bean2 as RtSessionConfig))
        }



        val bean = InjectFactory.getBean(RtSSLConfig::class.java)
        if (bean!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtSSLConfig = bean as RtSSLConfig))
        }

        val bean4 = InjectFactory.getBean(RtFileConfig::class.java)
        if (bean4!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtFileConfig = bean4 as RtFileConfig))
        }

        val bean5 = InjectFactory.getBean(RtTimeOutConfig::class.java)
        if (bean5!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtTimeOutConfig = bean5 as RtTimeOutConfig))
        }

        val bean6 = InjectFactory.getBean(RtInitConfig::class.java)
        if (bean6!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtInitConfig = bean6 as RtInitConfig))
        }

        val bean7 = InjectFactory.getBean(RtDataConverter::class.java)
        if (bean7!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtDataConverter = bean7 as RtDataConverter))
        }

        val bean8 = InjectFactory.getBean(RtResourcesConfig::class.java)
        if (bean8!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtResourcesConfig = bean8 as RtResourcesConfig))
        }
    }

}