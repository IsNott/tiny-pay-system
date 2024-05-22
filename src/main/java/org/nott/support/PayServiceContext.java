package org.nott.support;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.ArrayUtils;

import org.nott.annotations.PaymentType;
import org.nott.common.SpringContextUtils;
import org.nott.exception.PayException;
import org.nott.result.Result;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nott
 * @date 2024-5-15
 */

public class PayServiceContext {

    public static final ConcurrentHashMap<String,Class<?>> PAYMENT_SERVICE = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String,Method> REFUND_SERVICE = new ConcurrentHashMap<>();


    public static Object instanceService(String code){
        Class<?> serviceClass = PAYMENT_SERVICE.get(code);
        Objects.requireNonNull(serviceClass);
        Object object;
        try {
//            object = serviceClass.getDeclaredConstructor().newInstance();
            object = SpringContextUtils.getBean(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, serviceClass.getSimpleName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    public static Method findServiceMethod(String code, String payType) {
        Class<?> serviceClass = PAYMENT_SERVICE.get(code);
        Objects.requireNonNull(serviceClass);
        Method actMethod;
        try {
            Method[] methods = serviceClass.getDeclaredMethods();
            if (ArrayUtils.isEmpty(methods)) {
                throw new RuntimeException(String.format("%s Do not have any methods", serviceClass.getName()));
            }
            actMethod = Arrays.stream(methods).filter(r -> r.isAnnotationPresent(PaymentType.class) && payType.equals(r.getAnnotation(PaymentType.class).value()))
                    .findAny().orElse(null);
            Objects.requireNonNull(actMethod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return actMethod;
    }

    public static Result invokePaymentTypeMethod(String code, String type, Object... args) {
        Method serviceMethod = findServiceMethod(code, type);
        Object service = instanceService(code);
        Result result = null;
        try {
            result = (Result) serviceMethod.invoke(service, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Result invokeRefundMethod(String code,Object... args) {
        Method serviceMethod = REFUND_SERVICE.get(code);
        if(serviceMethod == null){
            throw new PayException(String.format("Payment Code [%s] can not found any refund method.", code));
        }
        Object service = instanceService(code);
        Result result = null;
        try {
            result = (Result) serviceMethod.invoke(service, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
