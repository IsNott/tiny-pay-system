package org.nott.common;

import org.nott.annotations.PaymentType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Nott
 * @date 2024-5-9
 */
public class ReflectUtils {

    public static Object invokePaymentTypeMethod(Class<?> clazz, String type, Object... args) {
        Method[] methods = clazz.getDeclaredMethods();
        Object result = null;
        Method method = Arrays.stream(methods)
                .filter(r -> r.getAnnotation(PaymentType.class) != null && r.getAnnotation(PaymentType.class).value().equals(type))
                .findAny().orElse(null);
        if (method != null) {
            try {
                Object o = clazz.getDeclaredConstructor().newInstance();
                result = method.invoke(o, args);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static Class<?> findClassByPaymentCode(String paymentCode) {
        // TODO
        return null;
    }
}
