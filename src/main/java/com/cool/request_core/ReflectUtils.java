package com.cool.request_core;



import com.cool.request_core.CombinationAnnotationElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public class ReflectUtils {
    public static boolean haveAnnotation(AnnotatedElement annotationEle, Class<? extends Annotation> annotationClass) {
        if (annotationEle == null || annotationClass == null) {
            return false;
        }
        return getAnnotation(annotationEle, annotationClass) != null;
    }

    public static <T extends Annotation> T getAnnotation(AnnotatedElement annotationEle, Class<T> annotationClass) {
        if (annotationEle == null || annotationClass == null) {
            return null;
        }
        CombinationAnnotationElement combinationAnnotationElement;
        if (annotationEle instanceof CombinationAnnotationElement) {
            combinationAnnotationElement = (CombinationAnnotationElement) annotationEle;
        } else {
            combinationAnnotationElement = new CombinationAnnotationElement(annotationEle);
        }
        return combinationAnnotationElement.getAnnotation(annotationClass);
    }

    /**
     * inInstance is error method
     */
    @Deprecated()
    public static boolean isBasicTypesObject(Class<?> clazz) {
        return clazz.isArray() ||
                clazz.isInterface() ||
                clazz.isEnum() ||
                clazz == (Number.class) ||
                clazz == (String.class) ||
                clazz == (Integer.class) ||
                clazz == (int.class) ||
                clazz == (Boolean.class) ||
                clazz == (boolean.class) ||
                clazz == (Double.class) ||
                clazz == (double.class) ||
                clazz == (Float.class) ||
                clazz == (float.class) ||
                clazz == (Long.class) ||
                clazz == (long.class) ||
                clazz == (byte.class) ||
                clazz == (short.class);
    }

    public static Object invokeMethod(Object clazz, Method method,Object...objects) throws InvocationTargetException, IllegalAccessException {
        if (clazz==null || method==null){
            return null;
        }
        return method.invoke(clazz, objects);
    }

    public static void injectField(Object clazz, Field field, Object value) throws IllegalAccessException {
        boolean isHidden = field.isAccessible();
        field.setAccessible(true);
        field.set(clazz,value);
        if(!isHidden){
            field.setAccessible(false);
        }
    }

    public static boolean isSameClass(Class<?> parent,Class<?> child){
        return parent.isAssignableFrom(child);
    }

    public static boolean isSameClass(Object parent,Object child){
        return isSameClass(parent.getClass(),child.getClass());
    }
}

