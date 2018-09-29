package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class TimedExecutors {
    public static TimedExecutor wrap(ScheduledExecutorService legacy) {
        Preconditions.checkNotNull(legacy);
        return new TimedExecutor() {
            @Override
            public void execute(TimedRunnable task) {
                legacy.schedule(() -> {
                    TimedRunnable next = task.run();
                    if (next != null) {
                        execute(next);
                    }
                }, task.when.toEpochMilli(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void close() {
                legacy.shutdown();
            }
        };
    }

    public static TimedThreadPool newTimedThreadPool(int threadCount) {
        return newTimedThreadPool(threadCount, Executors.defaultThreadFactory());
    }

    public static TimedThreadPool newTimedThreadPool(int threadCount, ThreadFactory threadFactory) {
        Preconditions.checkNotNull(threadFactory);
        TimedThreadPool pool = new TimedThreadPool();
        for (int i = 0; i < threadCount; ++i) {
            threadFactory.newThread(pool.new Worker(i)).start();
        }
        return pool;
    }

    private TimedExecutors() {
        throw new UnsupportedOperationException();
    }
}
