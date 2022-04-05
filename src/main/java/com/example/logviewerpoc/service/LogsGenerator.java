package com.example.logviewerpoc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LogsGenerator {
    private AtomicInteger logRecordCount = new AtomicInteger(0);

    public void generateLogs(int recordCount, int nThreads) {
           ExecutorService es = Executors.newFixedThreadPool(nThreads);
           final MutableInt i = new MutableInt(0);
           while (i.intValue() < nThreads) {
               es.submit(() -> {
                   while (logRecordCount.intValue() < recordCount) {
                       String message = "Current time: " + LocalDateTime.now();
                       switch (logRecordCount.intValue() % 4) {
                           case 0:
                             log.error(message);
                             break;
                           case 1:
                               log.warn(message);
                               break;
                           case 2:
                               log.info(message);
                               break;
                           case 3:
                               log.debug(message);
                               break;
                       }

                       if (logRecordCount.intValue() % 100 == 0) {
                           log.error("Oh my, exception!", new RuntimeException("Oh my"));
                       }
                       logRecordCount.incrementAndGet();
                   }

                   log.info ("Log generation completed in thread {}", Thread.currentThread());
               });
               i.increment();
           }

           es.shutdown();
    }
}
