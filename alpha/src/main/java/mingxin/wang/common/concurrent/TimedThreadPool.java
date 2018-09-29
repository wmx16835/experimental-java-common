package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class TimedThreadPool implements TimedExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimedThreadPool.class);
    private static final Comparator<TimedRunnable> PRIORITIZED_TASK_COMPARATOR = Comparator.comparing(o -> o.when);
    private static final Comparator<Worker> PENDING_WORKER_COMPARATOR = Comparator
            .comparing((Worker worker) -> worker.task.when)
            .reversed()
            .thenComparing(worker -> worker.id);

    private final Lock mutex;
    private final PriorityQueue<TimedRunnable> tasks;
    private final Queue<Worker> idle;
    private final TreeSet<Worker> pending;
    private boolean isShutdown;

    TimedThreadPool() {
        this.mutex = new ReentrantLock();
        this.tasks = new PriorityQueue<>(PRIORITIZED_TASK_COMPARATOR);
        this.idle = new ArrayDeque<>();
        this.pending = new TreeSet<>(PENDING_WORKER_COMPARATOR);
        this.isShutdown = false;
    }

    @Override
    public void execute(TimedRunnable task) {
        Preconditions.checkNotNull(task);
        Worker worker;
        mutex.lock();
        try {
            if (idle.isEmpty()) {
                Iterator<Worker> pendingIterator = pending.iterator();
                if (pendingIterator.hasNext() && (worker = pendingIterator.next()).task.when.compareTo(task.when) > 0) {
                    pendingIterator.remove();
                    tasks.add(worker.task);
                } else {
                    tasks.add(task);
                    return;
                }
            } else {
                worker = idle.poll();
            }
            worker.task = task;
            pending.add(worker);
            worker.condition.signal();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void close() {
        mutex.lock();
        try {
            isShutdown = true;
            idle.forEach(worker -> worker.condition.signal());
            pending.forEach(worker -> worker.condition.signal());
        } finally {
            mutex.unlock();
        }
    }

    final class Worker implements Runnable {
        private final int id;  // For performance of ordering
        private final Condition condition = mutex.newCondition();
        private TimedRunnable task;

        Worker(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            for (;;) {
                mutex.lock();
                if (isShutdown) {
                    return;
                }
                if (tasks.isEmpty()) {
                    task = null;
                    idle.offer(this);
                    do {
                        condition.awaitUninterruptibly();
                        if (isShutdown) {
                            return;
                        }
                    } while (task == null);
                } else {
                    task = tasks.poll();
                }
                for (;;) {
                    pending.add(this);
                    for (;;) {
                        try {
                            if (!condition.awaitUntil(Date.from(task.when))) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            LOGGER.error("Thread was interrupted while waiting for deadline", e);
                        }
                        if (isShutdown) {
                            return;
                        }
                    }
                    pending.remove(this);
                    mutex.unlock();
                    TimedRunnable next = task.run();
                    if (next == null) {
                        break;
                    }
                    mutex.lock();
                    if (!tasks.isEmpty() && tasks.peek().when.compareTo(next.when) < 0) {
                        task = tasks.poll();
                        tasks.offer(next);
                    } else {
                        task = next;
                    }
                }
            }
        }
    }
}
