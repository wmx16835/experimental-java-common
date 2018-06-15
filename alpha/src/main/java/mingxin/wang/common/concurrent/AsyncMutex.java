package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class AsyncMutex {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMutex.class);

    private final Executor executor;
    private final MutexQueue<Invocation> queue;
    private final AtomicInteger pendingCount;

    public static AsyncMutex on(Executor executor) {
        Preconditions.checkNotNull(executor);
        return new AsyncMutex(executor, new MutexQueue<>(), new AtomicInteger());
    }

    public <T> void attach(Callable<T> callable, Consumer<? super T> callback) {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(callback);
        queue.offer(new Invocation<>(callable, callback));
        if (pendingCount.getAndIncrement() == 0) {
            queue.pull().invoke(this);
        }
    }

    private void release() {
        if (pendingCount.decrementAndGet() != 0) {
            queue.pull().invoke(this);
        }
    }

    private static final class Invocation<T> {
        private final Callable<T> callable;
        private final Consumer<? super T> callback;

        private Invocation(Callable<T> callable, Consumer<? super T> callback) {
            this.callable = callable;
            this.callback = callback;
        }

        private void invoke(AsyncMutex mutex) {
            try {
                mutex.executor.execute(() -> {
                    T result;
                    try {
                        result = callable.call();
                    } catch (Throwable t) {
                        mutex.release();
                        LOGGER.error("Unexpected exception was caught while executing task, with invocation={}", this, t);
                        return;
                    }
                    mutex.release();
                    try {
                        callback.accept(result);
                    } catch (Throwable t) {
                        LOGGER.error("Unexpected exception was caught while executing callback, with result={}, invocation={}", result, this, t);
                    }
                });
            } catch (Throwable t) {
                mutex.release();
                LOGGER.error("Unexpected exception was caught while submitting the task to the executor, with invocation={}", this, t);
            }
        }

        @Override
        public String toString() {
            return "InvocationNode{" +
                    "callable=" + callable +
                    ", callback=" + callback +
                    '}';
        }
    }

    private AsyncMutex(Executor executor, MutexQueue<Invocation> queue, AtomicInteger pendingCount) {
        this.executor = executor;
        this.queue = queue;
        this.pendingCount = pendingCount;
    }
}
