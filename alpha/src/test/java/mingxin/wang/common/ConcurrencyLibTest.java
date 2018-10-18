package mingxin.wang.common;

import mingxin.wang.common.concurrent.ConcurrentInvoker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ConcurrencyLibTest {
    private static ExecutorService someExecutor;

    @BeforeClass
    public static void init() {
        someExecutor = Executors.newFixedThreadPool(8);
    }

    @AfterClass
    public static void deinit() {
        someExecutor.shutdown();
    }

    @Test
    public void testSyncInvoke() {
        ConcurrentInvoker invoker = new ConcurrentInvoker();
        for (int i = 0; i < 10; ++i) {
            invoker.add(someExecutor, () -> System.out.println("Hello World!"));
        }
        invoker.syncInvoke();
        System.out.println("Done");
    }

    @Test
    public void testInvoke() throws InterruptedException {
        ConcurrentInvoker invoker = new ConcurrentInvoker();
        for (int i = 0; i < 10; ++i) {
            invoker.add(someExecutor, () -> System.out.println("Hello World!"));
        }
        invoker.invoke(() -> System.out.println("Done"));
        Thread.sleep(1000);
    }

    @Test
    public void testFork() {
        ConcurrentInvoker invoker = new ConcurrentInvoker();
        invoker.add(new RepetitiveCallable(10 , () -> System.out.println("Hello World!")));
        invoker.syncInvoke();
        System.out.println("Done");
    }

    private static class RepetitiveCallable implements Consumer<ConcurrentInvoker.Breakpoint> {
        private static final int PARTITION_COUNT = 3;

        private int repetition;
        private Runnable task;

        RepetitiveCallable(int repetition, Runnable task) {
            this.repetition = repetition;
            this.task = task;
        }

        @Override
        public void accept(ConcurrentInvoker.Breakpoint breakpoint) {
            someExecutor.execute(() -> {
                splitAndExecute(breakpoint, repetition);
                breakpoint.join();
            });
        }

        private void splitAndExecute(ConcurrentInvoker.Breakpoint breakpoint, int repetition) {
            int newRepetition = repetition / PARTITION_COUNT;
            if (newRepetition > 0) {
                ConcurrentInvoker invoker = new ConcurrentInvoker();
                RepetitiveCallable forkTask = new RepetitiveCallable(newRepetition, task);
                for (int i = 0; i < PARTITION_COUNT - 1; ++i) {
                    invoker.add(forkTask);
                }
                invoker.fork(breakpoint);
                splitAndExecute(breakpoint, repetition - newRepetition * 2);
            } else {
                for (int i = 0; i < repetition; ++i) {
                    task.run();
                }
            }
        }
    }
}
