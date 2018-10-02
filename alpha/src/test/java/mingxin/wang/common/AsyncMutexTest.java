package mingxin.wang.common;

import mingxin.wang.common.concurrent.AsyncMutex;
import mingxin.wang.common.concurrent.DisposableBlocker;
import mingxin.wang.common.util.ResourceMonitor;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class AsyncMutexTest {
    // Test configurations
    private static final int THREAD_COUNT = 100;
    private static final int GROUP_COUNT = 100;
    private static final int TASKS_PER_GROUP_COUNT = 100;
    private static final long EXECUTION_MILLIS = 10;

    // Test thread pool
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    @AfterClass
    public static void postProcess() throws InterruptedException {
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.DAYS);
        System.out.println("Thread pool terminated.");
    }

    @Test
    public void testAsyncMutex() {
        System.out.println("Testing class AsyncMutex...");

        // 等待程序结束的阻塞对象
        DisposableBlocker blocker = new DisposableBlocker();

        AtomicInteger remain = new AtomicInteger(GROUP_COUNT);

        // 初始化异步互斥量
        AsyncMutex[] mutexes = new AsyncMutex[GROUP_COUNT];
        for (int i = 0; i < GROUP_COUNT; ++i) {
            mutexes[i] = AsyncMutex.on(threadPool);
        }

        int[] tasksDone = new int[GROUP_COUNT];

        ResourceMonitor monitor = new ResourceMonitor("Async Mutex");

        // 模拟多组并发操作
        for (int i = 0; i < GROUP_COUNT; ++i) {
            int group = i;
            for (int j = 0; j < TASKS_PER_GROUP_COUNT; ++j) {
                mutexes[group].attach(() -> {
                    doSomething();
                    return ++tasksDone[group] == TASKS_PER_GROUP_COUNT;
                }, done -> {
                    if (done && remain.decrementAndGet() == 0) {
                        monitor.record();
                        blocker.unblock();
                    }
                });
            }
        }

        // 等待程序结束
        blocker.block();
        System.out.println("Done.");
    }

    @Test
    public void testReentrantLock() {
        System.out.println("Testing class Reentrant Lock...");

        // 等待程序结束的阻塞对象
        DisposableBlocker blocker = new DisposableBlocker();

        AtomicInteger remain = new AtomicInteger(GROUP_COUNT);

        // 初始化异步互斥量
        ReentrantLock[] mutexes = new ReentrantLock[GROUP_COUNT];
        for (int i = 0; i < GROUP_COUNT; ++i) {
            mutexes[i] = new ReentrantLock();
        }

        int[] tasksDone = new int[GROUP_COUNT];

        ResourceMonitor monitor = new ResourceMonitor("Reentrant Lock");

        // 模拟多组并发操作
        for (int i = 0; i < GROUP_COUNT; ++i) {
            int group = i;
            for (int j = 0; j < TASKS_PER_GROUP_COUNT; ++j) {
                threadPool.execute(() -> {
                    boolean done;
                    mutexes[group].lock();
                    try {
                        doSomething();
                        done = ++tasksDone[group] == TASKS_PER_GROUP_COUNT;
                    } finally {
                        mutexes[group].unlock();
                    }
                    if (done && remain.decrementAndGet() == 0) {
                        monitor.record();
                        blocker.unblock();
                    }
                });
            }
        }

        // 等待程序结束
        blocker.block();
        System.out.println("Done.");
    }

    private void doSomething() {
        try {
            Thread.sleep(EXECUTION_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
