package org.example.concurrency.stock.distributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionTimedOutException;

import java.lang.reflect.Method;

@Slf4j
@Order(1) // @Transactional 보다 먼저 실행되어야함
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${enableDistributedLock:true}")
public class DistributedLockAspect {
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final RedissonClient redissonClient;
    private final CallTransactionFactory callTransactionFactory;


    @Around("@annotation(DistributedLock)")
    public Object applyDistributedLock(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        final DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        final String lockKey = createLockName(signature.getParameterNames(), proceedingJoinPoint.getArgs(), distributedLock);

        RLock rLock = redissonClient.getLock(lockKey);

        try {
            tryRedissonLock(rLock, distributedLock);
            return callTransactionFactory
                    .getCallTransaction(distributedLock.needSameTransaction())
                    .proceed(proceedingJoinPoint);
        } catch (TransactionTimedOutException timedOutException) {
            log.error("Transaction timeout! : {}", timedOutException.getMessage());
            throw timedOutException;
        } finally {
            tryUnlock(rLock);
        }
    }


    private void tryRedissonLock(final RLock rLock, final DistributedLock distributedLock) throws InterruptedException {
        long waitTime = distributedLock.waitTime(); // redisson default waitTime 이 30s
        long leaseTime = distributedLock.leaseTime();

        boolean available = rLock.tryLock(waitTime, leaseTime, distributedLock.unit());
        if (!available) {
            throw new RuntimeException("Failed to get a lock: " + rLock.getName());
        }
        log.debug("Lock acquired: {}, tid: {}", rLock.getName(), Thread.currentThread().getId());
    }

    private String createLockName(String[] parameterNames, Object[] args, DistributedLock distributedLock) {
        EvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Object value = EXPRESSION_PARSER.parseExpression(distributedLock.value()).getValue(context, Object.class);
        return distributedLock.key() + ":" + value;
    }

    private void tryUnlock(final RLock rLock) {
        try {
            rLock.unlock();
            log.debug("Unlocked: {}", rLock.getName());
        } catch (IllegalMonitorStateException e) {
            log.error("Failed to unlock: {}, {}", rLock.getName(), e.getMessage());
            throw e;
        }
    }

}
