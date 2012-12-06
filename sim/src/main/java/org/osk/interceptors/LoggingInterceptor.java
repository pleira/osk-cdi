package org.osk.interceptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;


@Log
@Interceptor
public class LoggingInterceptor {

    @Inject Logger logger;
//            java.util.logging.Logger.getLogger("theLogger");

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        final Method method = context.getMethod();
        if (method.isAnnotationPresent(Info.class)) {
            return info(context);
        } else if (method.isAnnotationPresent(Debug.class)) {
            return debug(context);
        } else {
            return info(context);
//            return trace(context);
        }
    }

    public Object info(InvocationContext context) throws Exception {
        logger.info("" + context.getMethod().getName());
        Annotation[][] parameterAnnotations = context.getMethod().getParameterAnnotations();
        Object[] parameterValues = context.getParameters();
        Class<?>[] parameterTypes = context.getMethod().getParameterTypes();
        
        for (int index = 0; index < parameterValues.length; index++) {
            logger.info("param {} value={}", index, parameterValues[index]);
            logger.info("type={} annotations={} \n", 
               parameterTypes[index], Arrays.toString(parameterAnnotations[index]));
        }
        return context.proceed();
    }

    public Object debug(InvocationContext context) throws Exception {
        logger.debug("" + context.getMethod().getName());
        Annotation[][] parameterAnnotations = context.getMethod().getParameterAnnotations();
        Object[] parameterValues = context.getParameters();
        Class<?>[] parameterTypes = context.getMethod().getParameterTypes();
        
        for (int index = 0; index < parameterValues.length; index++) {
            logger.debug("param {} value={}", index, parameterValues[index]);
            logger.debug("type={} annotations={} \n", 
               parameterTypes[index], Arrays.toString(parameterAnnotations[index]));
        }
        return context.proceed();
    }

    public Object trace(InvocationContext context) throws Exception {
        logger.trace("" + context.getMethod().getName());
        return context.proceed();
    }
}