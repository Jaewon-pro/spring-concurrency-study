package org.example.concurrency.stock.facade;

import lombok.RequiredArgsConstructor;
import org.example.concurrency.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class OptimisticLockFacade {


    private final OptimisticLockStockService optimisticLockStockService;


//    public void decrease(Long id, Long quantity) throws InterruptedException {
//        retry((AA aa) -> {
//            optimisticLockStockService.decrease(aa.id, aa.quantity);
//            return null;
//        }, new AA(id, quantity), Duration.ofMillis(50));
//    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }


    private <T, R> R retry(Function<T, R> function, T t, Duration waitFor) throws InterruptedException {
        while (true) {
            try {
                return function.apply(t);
            } catch (Exception e) {
                Thread.sleep(waitFor.toMillis());

            }
        }
    }

    record AA(
            Long id, Long quantity
    ) {
    }


}
