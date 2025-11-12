package com.dev.wizard.cache.redis;

import com.dev.wizard.cache.redis.anno.RedisIdempotent;
import com.dev.wizard.cache.redis.anno.RedisLock;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class RedisLockAspect {
    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    private RedisManager redisManager;



    @Around("@annotation(com.dev.wizard.cache.redis.anno.RedisIdempotent)")
    public Object idempotentAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSign = (MethodSignature)joinPoint.getSignature();
        Method method = methodSign.getMethod();
        RedisIdempotent idempotentAnno = method.getAnnotation(RedisIdempotent.class);
        if(idempotentAnno == null || StringUtil.isBlank(idempotentAnno.key())){
            log.warn("key is null or empty, executing business logic without idempotency guarantee");
            return joinPoint.proceed();
        }
        String key = parseKey(idempotentAnno.key(), method, joinPoint.getArgs());
        long expire = idempotentAnno.expireTime();
        return redisManager.executeWithIdempotent(key, expire, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                if (t instanceof RuntimeException) throw (RuntimeException)t;
                if (t instanceof Error) throw (Error)t;
                throw new RuntimeException(t);
            }
        });
    }

    @Around("@annotation(com.dev.wizard.cache.redis.anno.RedisLock)")
    public Object around(ProceedingJoinPoint joinPoint){
        MethodSignature methodSign = (MethodSignature)joinPoint.getSignature();
        Method method = methodSign.getMethod();
        RedisLock redisLockAnnotation = method.getAnnotation(RedisLock.class);
        String key = parseKey(redisLockAnnotation.key(), method, joinPoint.getArgs());
        long lockExpiredTime = redisLockAnnotation.lockExpiredTime();
        long tryLockTime = redisLockAnnotation.tryLockTime();
        return redisManager.executeWithRedisLock(key, lockExpiredTime, tryLockTime, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                if (t instanceof RuntimeException) throw (RuntimeException)t;
                if (t instanceof Error) throw (Error)t;
                throw new RuntimeException(t);
            }
        });
    }
    private String parseKey(String keyExpression, Method method, Object[] args) {
        if (!keyExpression.contains("#")) {
            return keyExpression; // 普通字符串
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
