package mingxin.wang.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.Lists;
import mingxin.wang.common.concurrent.AsyncMutex;
import mingxin.wang.common.concurrent.DisposableBlocker;
import mingxin.wang.common.context.NumberedReducible;
import mingxin.wang.common.context.NumberedReducingPortion;
import mingxin.wang.common.context.Reduced;
import mingxin.wang.common.context.ReducingContext;
import mingxin.wang.common.context.ReducingContextService;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ReducingTest {
    // 测试配置
    private static final int THREAD_COUNT = 3000;
    private static final int TASK_COUNT = 10000;
    private static final long SLEEP_MILLIS = 3000;

    // 测试线程池
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    @AfterClass
    public static void postProcess() throws InterruptedException {
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.DAYS);
        System.out.println("Thread pool terminated.");
    }

    @Test
    public void testAsyncMonitor() {
        System.out.println("Testing class AsyncMonitor...");

        // 创建异步管程
        AsyncMonitor<List<Integer>> monitor = new AsyncMonitor<>(Lists.newArrayList());

        // 等待程序结束的阻塞对象
        DisposableBlocker blocker = new DisposableBlocker();

        // 模拟并发操作
        for (int i = 1; i <= TASK_COUNT; ++i) {
            int current = i;
            threadPool.execute(() -> {
                doSomething();
                monitor.invoke(result -> {
                    result.add(current);
                    if (result.size() == TASK_COUNT) {
                        System.out.println("Size: " + result.size());
                        System.out.println(result);
                        blocker.unblock();
                    }
                });
            });
        }

        // 等待程序结束
        blocker.block();

        // 停止异步管程
        monitor.stop();
        System.out.println("Done.");
    }

    @Test
    public void testAsyncMutex() {
        System.out.println("Testing class AsyncMutex...");

        // 共享数据
        List<Integer> data = Lists.newArrayList();

        // 创建异步互斥量
        AsyncMutex mutex = AsyncMutex.on(threadPool);

        // 等待程序结束的阻塞对象
        DisposableBlocker blocker = new DisposableBlocker();

        // 模拟并发操作
        for (int i = 1; i <= TASK_COUNT; ++i) {
            int current = i;
            threadPool.execute(() -> {
                doSomething();
                mutex.attach(() -> {
                    data.add(current);
                    return data.size() == TASK_COUNT;
                }, done -> {
                    if (done) {
                        System.out.println("Size: " + data.size());
                        System.out.println(data);
                        blocker.unblock();
                    }
                });
            });
        }

        // 等待程序结束
        blocker.block();
        System.out.println("Done.");
    }

    @Test
    public void testReducingContextService() throws InterruptedException {
        System.out.println("Testing class ReducingContext...");

        // 构造供上下文服务使用的缓存
        Cache<String, ReducingContext<NumberedReducingPortion<Integer>, Collection<Integer>>> contextCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .removalListener(notification -> {
                    if (notification.getCause() == RemovalCause.EXPIRED) {
                        System.out.println("Context was removed due to timeout, id=" + notification.getKey());
                    }
                })
                .build();

        // 等待程序结束的阻塞对象
        DisposableBlocker blocker = new DisposableBlocker();

        // 构造将 NumberedReducingPortion<Integer> 合并为 List<Integer> 的 Reducing 上下文管理器，纯异步且线程安全
        ReducingContextService<String, NumberedReducingPortion<Integer>, Collection<Integer>> contextService =
                new ReducingContextService<String, NumberedReducingPortion<Integer>, Collection<Integer>>(contextCache) {
                    @Override
                    protected void postProcess(String id, Reduced<? extends Collection<Integer>> result) {
                        if (result.isLast()) {
                            Collection<Integer> entirety = result.getData();
                            System.out.println("Size: " + entirety.size());
                            System.out.println(entirety);
                            blocker.unblock();
                        }
                    }
                };

        // 多次回调 Reduce 逻辑，由 Integer 合并到上下文 List<Integer>，不需保证线程安全
        NumberedReducible<Integer, Collection<Integer>, List<Integer>> reducible = new NumberedReducible<Integer, Collection<Integer>, List<Integer>>(Lists.newArrayList()) {
            @Override
            protected Collection<Integer> reduce(NumberedReducingPortion<Integer> portion, List<Integer> entirety) {
                entirety.add(portion.getData());
                return portion.isLast() ? entirety : null;
            }
        };

        // 将 reducible 注册到上下文服务
        contextService.register("test", threadPool, reducible);

        // 初始化模拟数据源并乱序
        ArrayList<Integer> source = new ArrayList<>();
        for (int i = 1; i <= TASK_COUNT; ++i) {
            source.add(i);
        }
        Collections.shuffle(source);

        // 模拟多次回调
        for (int i = 1; i <= TASK_COUNT; ++i) {
            int sequence = i;
            threadPool.execute(() -> {
                doSomething();
                Optional<ReducingContext<NumberedReducingPortion<Integer>, Collection<Integer>>> contextOptional = contextService.get("test");
                if (contextOptional.isPresent()) {
                    NumberedReducingPortion<Integer> portion = new NumberedReducingPortion<>(sequence, sequence == TASK_COUNT, source.get(sequence - 1));
                    contextOptional.get().reduce(portion);
                } else {
                    System.out.println("Context not found");
                }
            });
        }

        // 等待程序结束
        blocker.block();
        System.out.println("Done.");
    }

    private void doSomething() {
        try {
            Thread.sleep((long) (Math.random() * SLEEP_MILLIS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
