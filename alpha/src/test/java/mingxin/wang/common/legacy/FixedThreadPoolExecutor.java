package mingxin.wang.common.legacy;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class FixedThreadPoolExecutor extends AbstractExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedThreadPoolExecutor.class);

    private ReentrantLock mutex = new ReentrantLock();
    private Condition condition = mutex.newCondition();
    private boolean isShutdown = false;
    private Queue<Runnable> tasks = new ArrayDeque<>();

    public FixedThreadPoolExecutor(int threadCount) {
        this(threadCount, Executors.defaultThreadFactory());
    }

    public FixedThreadPoolExecutor(int threadCount, ThreadFactory threadFactory) {
        for (int i = 0; i < threadCount; ++i) {
            threadFactory.newThread(() -> {
                mutex.lock();
                for (;;) {
                    if (!tasks.isEmpty()) {
                        Runnable task = tasks.poll();
                        mutex.unlock();
                        try {
                            task.run();
                        } catch (Throwable t) {
                            LOGGER.error("Exception was caught while executing a task.", t);
                        }
                        mutex.lock();
                    } else if (isShutdown) {
                        break;
                    } else {
                        condition.awaitUninterruptibly();
                    }
                }
                mutex.unlock();
            }).start();
        }
    }

    @Override
    public void shutdown() {
        mutex.lock();
        isShutdown = true;
        condition.signalAll();
        mutex.unlock();
    }

    @Override
    public List<Runnable> shutdownNow() {
        mutex.lock();
        isShutdown = true;
        try {
            ArrayList<Runnable> result = Lists.newArrayList(tasks);
            tasks.clear();
            condition.signalAll();
            return result;
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Runnable command) {
        mutex.lock();
        try {
            tasks.add(command);
            condition.signal();
        } finally {
            mutex.unlock();
        }
    }
}
