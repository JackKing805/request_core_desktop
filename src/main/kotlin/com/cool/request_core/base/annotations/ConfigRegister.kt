package com.cool.request_core.base.annotations

import kotlin.reflect.KClass

/**
 * 配置注册注解，需要搭配IConfig同时使用
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Bean
annotation class ConfigRegister(
  val priority:Int = 0,//优先级，数字越大越先被调用
  val registerClass:KClass<*> = Any::class
)
