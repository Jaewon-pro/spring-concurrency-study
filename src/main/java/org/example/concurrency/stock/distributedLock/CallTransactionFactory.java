package org.example.concurrency.stock.distributedLock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CallTransactionFactory {

    private final RedissonCallSameTransaction redissonCallSameTransaction;
    private final RedissonCallNewTransaction redissonCallNewTransaction;

    public CallTransaction getCallTransaction(boolean needSame) {
        if (needSame) {
            return redissonCallSameTransaction;
        }
//        return new CallTransaction() {
//            @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 9)
//            public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
//                return joinPoint.proceed();
//            }
//        };
        return redissonCallNewTransaction;
    }

}
