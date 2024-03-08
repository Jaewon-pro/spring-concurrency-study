package org.example.concurrency.stock.distributedLock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락을 걸기 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();

    String value();

    boolean needSameTransaction() default false;

    long waitTime() default 10L;

    long leaseTime() default 10L;

    TimeUnit unit() default TimeUnit.SECONDS;
}
