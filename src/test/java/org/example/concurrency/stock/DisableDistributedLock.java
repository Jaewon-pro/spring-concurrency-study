package org.example.concurrency.stock;

import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

/**
 * 분산 락을 걸지 않기 위한 어노테이션
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {"enableDistributedLock=false"})
@Documented
public @interface DisableDistributedLock {
}
