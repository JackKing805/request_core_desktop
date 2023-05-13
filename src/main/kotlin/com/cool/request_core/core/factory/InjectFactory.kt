package com.cool.request_core.core.factory

import com.cool.request_core.base.annotations.*
import com.cool.request_core.base.bean.RequestMethod
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.additation.DefaultAuthConfigRegister
import com.cool.request_core.core.additation.DefaultResourcesDispatcherConfigRegister
import com.cool.request_core.core.additation.DefaultRtConfigRegister
import com.cool.request_core.core.additation.DefaultRtInitConfigRegister
import com.cool.request_core.core.anno.ParamsQuery
import com.cool.request_core.core.exception.IllPathException
import com.cool.request_core.core.exception.InitErrorException
import com.cool.request_core.core.extensions.getJustPath
import com.cool.request_core.core.extensions.isBasicType
import com.cool.request_core.core.utils.reflect.ReflectUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * 反射工具
 */
internal object InjectFactory {
    private val beans = mutableListOf<BeanMapper>()
    private val controllerMappers = mutableListOf<ControllerMapper>()
    private var defaultIsInit = false
    private val defaultInjects = mutableListOf<Class<*>>(
        DefaultAuthConfigRegister::class.java,
        DefaultResourcesDispatcherConfigRegister::class.java,
        DefaultRtConfigRegister::class.java,
        DefaultRtInitConfigRegister::class.java
    )

    fun inject(mutableList: MutableList<Class<*>>) {
        if (!defaultIsInit) {
            defaultIsInit = true
            mutableList.addAll(defaultInjects)
        }

        mutableList.forEach {
            val injectAnnotation = ReflectUtils.getAnnotation(it, Bean::class.java)
            if (injectAnnotation != null) {
                val ins = it.newInstance()
                initBeanClazz(ins)
                beans.add(BeanMapper(injectAnnotation.name, ins))
            }
        }

        initConfig()

        initController(mutableList)

        injectBeans()
    }


    private fun initBeanClazz(any: Any) {
        initBeanField(any)
        initBeanMethod(any, any::class.java.declaredMethods, mutableListOf())
    }

    private fun initBeanField(any: Any) {
        any::class.java.declaredFields.forEach {
            val bean = ReflectUtils.getAnnotation(it, Bean::class.java)
            if (bean != null) {
                it.isAccessible = true
                val r = it.get(any)
                if (r != null) {
                    beans.add(BeanMapper(bean.name, r))
                }
            }
        }
    }

    private fun initBeanMethod(
        any: Any,
        methods: Array<Method>,
        aInvokeMethods: MutableList<Method>
    ) {
        methods.forEach { it ->
            if (!aInvokeMethods.contains(it)) {
                val bean = ReflectUtils.getAnnotation(it, Bean::class.java)
                if (bean != null) {
                    val ps = it.parameters
                    val args = mutableListOf<Any>()
                    ps.forEach {
                        val beanI = getBeanByInjectOrClass(it, it.type)

                        if (beanI != null) {
                            args.add(beanI)
                        } else {
                            val method = methods.find { a ->
                                ReflectUtils.isSameClass(
                                    it.type,
                                    a.returnType
                                )
                            }
                            if (method != null) {
                                initBeanMethod(any, arrayOf(method), aInvokeMethods)
                                val beanI2 = getBeanByInjectOrClass(it, it.type)
                                if (beanI2 != null) {
                                    args.add(beanI2)
                                } else {
                                    throw NullPointerException("Please provider $it's bean")
                                }
                            } else {
                                throw NullPointerException("Please provider $it's bean")
                            }
                        }
                    }

                    val r = it.invoke(any, *args.toTypedArray())
                    aInvokeMethods.add(it)
                    if (r != null) {
                        beans.add(BeanMapper(bean.name, r))
                    }
                }
            }
        }
    }

    //根据已有的配置注册器注册配置，如若没有对应的配置注册器，就抛弃配置
    private fun initConfig() {
        val configurations = listContainsAnnotation(Configuration::class.java).map {
            ConfigurationMapper(
                it.bean,
                ReflectUtils.getAnnotation(it.bean::class.java, Configuration::class.java)
            )
        }
        val registers = getConfigRegisters()

        registers.forEach {
            it.instance.onCreate()
        }

        configurations.forEach { o ->
            registers.forEach { i ->
                if (ReflectUtils.isSameClass(
                        i.annotation.registerClass.java,
                        o.instance::class.java
                    )
                ) {
                    i.instance.init(o.annotation, o.instance)
                }
            }
        }
    }

    private fun initController(mutableList: MutableList<Class<*>>) {
        val findPathParams = "(^.*)\\{(.*?)\\}"
        val pathPattern = Pattern.compile(findPathParams)

        mutableList.filter { ReflectUtils.haveAnnotation(it, Controller::class.java) }.forEach {
            val controllerAnnotation = ReflectUtils.getAnnotation(it, Controller::class.java)
            if (controllerAnnotation != null) {
                val isClassJson = controllerAnnotation.isRest
                val clazzPath = controllerAnnotation.value
                val clazzEndIsLine = clazzPath.endsWith("/")
                it.declaredMethods.forEach { m ->
                    ReflectUtils.getAnnotation(m, Controller::class.java)?.let { mc ->
                        val isMethodJson = mc.isRest
                        val methodPath = if (clazzEndIsLine) {
                            if (mc.value.startsWith("/")) {
                                mc.value.substring(1)
                            } else {
                                mc.value
                            }
                        } else {
                            if (mc.value.startsWith("/")) {
                                mc.value
                            } else {
                                "/" + mc.value
                            }
                        }


                        val fullPath = clazzPath + methodPath
                        val matcher = pathPattern.matcher(fullPath)
                        val controllerMapper = if (matcher.find()) {
                            val path = matcher.group(1)!!
                            val param = matcher.group(2)!!
                            if (!path.endsWith("/")) {
                                throw IllPathException(fullPath)
                            }

                            val realPath = if (path.length == 1) {
                                path
                            } else {
                                path.substring(0, path.length - 1)
                            }

                            m.parameters.forEach { mp ->
                                if (ReflectUtils.haveAnnotation(
                                        mp,
                                        ParamsQuery::class.java
                                    ) && !mp.type.isBasicType()
                                ) {
                                    throw InitErrorException("ParamsQuery must be basic type")
                                }
                            }

                            ControllerMapper(
                                it,
                                m,
                                mc.requestMethod,
                                isClassJson or isMethodJson,
                                path = realPath,
                                ControllerPathParams(
                                    param
                                )
                            )
                        } else {
                            ControllerMapper(
                                it,
                                m,
                                mc.requestMethod,
                                isClassJson or isMethodJson,
                                fullPath
                            )
                        }

                        controllerMappers.add(
                            controllerMapper
                        )
                    }
                }
            }
        }
    }

    private fun injectBeans() {
        beans.forEach {
            injectField(it.bean)
        }
    }

    private fun injectField(any: Any) {
        any::class.java.declaredFields.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it, Inject::class.java)
            if (haveInject) {
                it.isAccessible = true
                if (it.isAccessible) {
                    val bean = getBeanByInjectOrClass(it, it.type)
                    if (bean != null) {
                        it.set(any, bean)
                    } else {
                        throw NullPointerException("please provider bean:${it.type}")
                    }
                }
            }
        }
    }


    fun getControllers() = controllerMappers

    fun getController(path: String): ControllerMapper? {
        val regex = "(^.*)\\/(.*)"
        val compile = Pattern.compile(regex)


        val controllers = getControllers()

        fun fullCheck(controllerPath: String, path: String): Boolean {
            val justPath = path.getJustPath()
            return justPath == controllerPath
        }

        controllers.forEach { t ->
            if (fullCheck(t.path, path)) {
                return t
            }
        }
        val matcher = compile.matcher(path)
        if (matcher.find()) {
            val rPath = matcher.group(1)!!
            return controllers.find { it.path == rPath }
        }
        return null
    }

    fun getConfigRegisters() = listContainsBy {
        ReflectUtils.haveAnnotation(
            it.bean::class.java,
            ConfigRegister::class.java
        ) && ReflectUtils.isSameClass(IConfig::class.java, it.bean::class.java)
    }.map {
        ConfigRegisterMapper(
            it.bean as IConfig,
            ReflectUtils.getAnnotation(it.bean::class.java, ConfigRegister::class.java)
        )
    }.sortedByDescending { it.annotation.priority }

    fun listContainsAnnotation(annotationClass: Class<out Annotation>) = listContainsBy {
        ReflectUtils.haveAnnotation(it.bean::class.java, annotationClass)
    }

    fun listContainsBy(condition: (BeanMapper) -> Boolean) = beans.filter { condition(it) }

    fun getBeanBy(condition: (BeanMapper) -> Boolean): BeanMapper? =
        listContainsBy(condition).firstOrNull()

    fun getBeanByInjectOrClass(annotatedElement: AnnotatedElement, clazz: Class<*>) = getBeanBy {
        val inject = ReflectUtils.getAnnotation(annotatedElement, Inject::class.java)
        if (inject != null && inject.name.isNotEmpty() && it.beanName.isNotEmpty()) {
            it.beanName == inject.name
        } else {
            ReflectUtils.isSameClass(clazz, it.bean::class.java)
        }
    }?.bean

    fun getBean(clazz: Class<*>): Any? {
        val list = listContainsBy {
            ReflectUtils.isSameClass(clazz, it.bean::class.java)
        }
        if (list.isEmpty()) {
            return null
        }
        list.forEach {
            if (it.bean::class.java == clazz) {
                return it.bean
            }
        }
        return list.firstOrNull()?.bean
    }

    fun getBean(beanName: String): Any? {
        return listContainsBy {
            if (it.beanName.isNotEmpty()) {
                it.beanName == beanName
            } else {
                false
            }
        }.firstOrNull()?.bean
    }

    fun <T : Annotation> getAnnotationBean(annotationClass: Class<T>): Any? {
        return getBeanBy {
            ReflectUtils.haveAnnotation(it.bean::class.java, annotationClass)
        }?.bean
    }

    //手动移除bean
    internal fun removeBean(bean: Any) {
        beans.removeAll {
            it.bean == bean
        }
    }

    //手动注入bean
    internal fun insertBean(bean: Any) {
        if (beans.find { it.bean == bean } == null) {
            beans.add(BeanMapper(bean.javaClass.simpleName, bean))
        }
    }
}

internal data class BeanMapper(
    val beanName: String,
    val bean: Any
)

internal data class ConfigRegisterMapper(
    val instance: IConfig,
    val annotation: ConfigRegister
)

internal data class ConfigurationMapper(
    val instance: Any,
    val annotation: Configuration
)

internal data class ControllerMapper(
    val clazz: Class<*>,
    val method: Method,
    val requestMethod: RequestMethod,
    val isRestController: Boolean,
    val path: String,
    val pathParam: ControllerPathParams? = null,
)

internal data class ControllerPathParams(
    val name: String
)
