package org.example.concurrency.stock.distributedLock;

import org.aspectj.lang.ProceedingJoinPoint;

public interface CallTransaction {
    Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable;
}
