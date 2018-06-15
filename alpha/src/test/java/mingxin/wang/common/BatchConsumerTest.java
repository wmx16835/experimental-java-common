package mingxin.wang.common;

import mingxin.wang.common.concurrent.BatchConsumer;
import mingxin.wang.common.concurrent.TimedExecutors;
import mingxin.wang.common.concurrent.TimedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class BatchConsumerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConsumerTest.class);
    private static final int BATCH_SIZE = 5;
    private static final Duration GAP = Duration.ofSeconds(5);
    private static final int POOL_SIZE = 2;

    public static void main(String[] args) {
        try (TimedThreadPool executor = TimedExecutors.newTimedThreadPool(POOL_SIZE);
             Scanner scanner = new Scanner(System.in)) {
            BatchConsumer<String> consumer = new BatchConsumer<String>(BATCH_SIZE, GAP, executor) {
                @Override
                protected void consume(List<String> data) {
                    LOGGER.info("size={}, data={}", data.size(), data);
                }
            };
            String s;
            while (!(s = scanner.next()).equals("x")) {
                consumer.accept(s);
            }
        }
    }
}
