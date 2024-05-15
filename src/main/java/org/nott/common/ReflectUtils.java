package org.nott.common;

import org.nott.annotations.Payment;
import org.nott.annotations.PaymentType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Nott
 * @date 2024-5-9
 */
public class ReflectUtils {


//    public static void processPayment(String packageName, String paymentType) throws Exception {
//        // 加载服务实现类
//        Class<?> serviceClass = Class.forName(serviceClassName);
//
//        // 获取支付服务类上的 @Payment 注解
//        Payment paymentAnnotation = serviceClass.getAnnotation(Payment.class);
//
//        if (paymentAnnotation != null) {
//            // 获取支付服务名称
//            String paymentName = paymentAnnotation.code();
//
//            // 根据支付方式获取对应的支付方法
//            Method[] methods = serviceClass.getDeclaredMethods();
//            for (Method method : methods) {
//                PaymentType paymentTypeAnnotation = method.getAnnotation(PaymentType.class);
//                if (paymentTypeAnnotation != null && paymentTypeAnnotation.value().equals(paymentType)) {
//                    // 创建支付服务实例
//                    Object serviceInstance = serviceClass.getDeclaredConstructor().newInstance();
//                    // 调用支付方法
//                    method.invoke(serviceInstance);
//                    return;
//                }
//            }
//        }
//        // 如果找不到对应的支付服务或支付方式，则抛出异常或执行相应的处理逻辑
//        throw new IllegalArgumentException("Invalid payment service or payment type");
//    }

    public static Class<?> findClassByPaymentCode(String paymentCode) {
        // TODO
        return null;
    }
}
