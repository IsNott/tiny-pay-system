package org.nott.scanner;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nott.annotations.Payment;
import org.nott.annotations.PaymentType;
import org.nott.exception.PayException;
import org.nott.service.AbstractPaymentService;
import org.nott.support.PayServiceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * payment注解扫描组件
 * @author Nott
 * @date 2024-5-15
 */
@Component
public class PaymentServiceScanner {

    @Value("${payment.package.name}")
    String basePackage;

    @PostConstruct
    public void scanPaymentServices() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Payment.class));
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage);
        if(CollectionUtils.isEmpty(beanDefinitions)){
            throw new PayException("Payment Service With Payment Annotation Not Found");
        }
        // 扫描指定包下的所有类
        for (BeanDefinition beanDefinition : beanDefinitions ) {
            try {
                String beanClassName = beanDefinition.getBeanClassName();
                Objects.requireNonNull(beanClassName);
                Class<?> clazz = Class.forName(beanClassName);
                Payment annotation = clazz.getAnnotation(Payment.class);
                String paymentValue = annotation.value();
                String paymentCode = annotation.code();
                PayServiceContext.PAYMENT_SERVICE.put(StringUtils.isNotEmpty(paymentValue) ? paymentValue : paymentCode, clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Payment Service Put In Context Throw Exception:{}", e.getException());
            }
        }
    }

}
