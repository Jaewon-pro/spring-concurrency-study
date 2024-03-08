package org.example.concurrency.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.concurrency.stock.Stock;
import org.example.concurrency.stock.distributedLock.DistributedLock;
import org.example.concurrency.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AopLockService {
    private final StockRepository stockRepository;

//    https://xerar.tistory.com/118 다양한(?) 동시성 제어 방법
    @DistributedLock(key = "stock", value = "#id")
    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 id의 stock이 없습니다."));
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

}
