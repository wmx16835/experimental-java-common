package mingxin.wang.common;

import mingxin.wang.common.concurrent.ConcurrentInvoker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ConcurrencyDemo {
    public static void helloWorld1(int n) throws InterruptedException {
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; ++i) {
            threads[i] = new Thread(() -> System.out.println("Hello World!"));
            threads[i].start();
        }
        for (int i = 0; i < n; ++i) {
            threads[i].join();
        }
        System.out.println("Done");
    }

    public static void helloWorld2(int n, ExecutorService executorService) throws ExecutionException, InterruptedException {
        Future[] futures = new Future[n];
        for (int i = 0; i < n; ++i) {
            futures[i] = executorService.submit(() -> System.out.println("Hello World!"));
        }
        for (int i = 0; i < n; ++i) {
            futures[i].get();
        }
        System.out.println("Done");
    }

    public static void helloWorld3(int n, Executor executor) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(n);
        for (int i = 0; i < n; ++i) {
            executor.execute(() -> {
                System.out.println("Hello World!");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("Done");
    }

    public static void helloWorld4(int n, Executor executor) throws InterruptedException {
        AtomicInteger count = new AtomicInteger(n);
        Thread currentThread = Thread.currentThread();
        for (int i = 0; i < n; ++i) {
            executor.execute(() -> {
                System.out.println("Hello World!");
                if (count.getAndDecrement() == 1) {
                    LockSupport.unpark(currentThread);
                }
            });
        }
        while (count.get() != 0) {
            LockSupport.park();
        }
        System.out.println("Done");
    }

    public static void helloWorld5(int n, Executor executor) throws InterruptedException {
        ConcurrentInvoker invoker = new ConcurrentInvoker();
        for (int i = 0; i < n; ++i) {
            invoker.add(executor, () -> System.out.println("Hello World!"));
        }
        invoker.syncInvoke();
        System.out.println("Done");
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        helloWorld5(10, threadPool);
        threadPool.shutdown();
    }
}
