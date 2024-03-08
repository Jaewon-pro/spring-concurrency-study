package org.example.concurrency.stock.service;

import org.example.concurrency.stock.DisableDistributedLock;
import org.example.concurrency.stock.Stock;
import org.example.concurrency.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisableDistributedLock // 분산락 끄기
@SpringBootTest
class AopLockServiceFailTest {

    @Autowired
    private AopLockService aopLockService;

    @Autowired
    private StockRepository stockRepository;


    @BeforeEach
    public void insert() {
        Stock stock = new Stock(1L, 100L);
        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void delete() {
        stockRepository.deleteAll();
    }


    @DisplayName("분산락 끄고 실행하면 재고 불일치, 동시에 100개의 요청하면 재고가 정확하게 감산 안됨")
    @Test
    void decreaseFail() throws InterruptedException { // 0.5초 정도 걸림
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    aopLockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 분산락 꺼서 실패하는게 정상
        System.out.println("stock.getQuantity() = " + stock.getQuantity());
        assertNotEquals(0, stock.getQuantity());
    }

}
