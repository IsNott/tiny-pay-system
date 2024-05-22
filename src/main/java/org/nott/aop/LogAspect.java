package org.nott.aop;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Nott
 * @date 2024-5-9
 */

@Aspect
@Component
public class LogAspect {

    private final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * org.nott.controller..*.*(..))")
    public void privilege() {}

    /**
     * 环绕通知
     *
     * @param pjd
     * @return
     * @throws Throwable
     */
    @Around("privilege()")
    public Object arount(ProceedingJoinPoint pjd) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = pjd.getTarget().getClass().getName();
        String methodName = pjd.getSignature().getName();

        Object[] args = pjd.getArgs();
       if(args.length > 0){
           try {
               String params = JSON.toJSONString(args[0]);
               log.info("{}.{}()[Method Param]：{}", className, methodName, params);
           } catch (Exception e) {
               log.info("{}.{}()[Method Param Print Error]：{}", className, methodName, e);
           }
       }
        Object result = pjd.proceed();
        try {
            String s = JSON.toJSONString(result);
            log.info("{}.{}()[Method Result]：{}", className, methodName, s);
        } catch (Exception e) {
            log.info("{}.{}()[Method Result Print Error]：{}", className, methodName, e);
        }
        long time = System.currentTimeMillis() - startTime;
        log.info("{}.{}()[Method Executed]：{}{}", className, methodName, time, " ms");
        return result;
    }
}
