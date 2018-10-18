package mingxin.wang.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class CirculationTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(CirculationTrigger.class);
    private static final int VERSION_MASK = 0x3FFFFFFF;
    private static final int RUNNING_FLAG_MASK = 0x40000000;
    private static final int RESERVATION_FLAG_MASK = 0x80000000;

    private final AtomicInteger state;
    private final ScheduledExecutorService executor;
    private final CirculatingRunnable task;

    public static CirculationTrigger bind(ScheduledExecutorService executor, CirculatingRunnable task) {
        return new CirculationTrigger(new AtomicInteger(0), executor, task);
    }

    public void fire() {
        fire(Duration.ZERO);
    }

    public void fire(Duration delay) {
        schedule(new Task(advanceVersion()), delay);
    }

    public void suspend() {
        advanceVersion();
    }

    private CirculationTrigger(AtomicInteger state, ScheduledExecutorService executor, CirculatingRunnable task) {
        this.state = state;
        this.executor = executor;
        this.task = task;
    }

    private int advanceVersion() {
        int s, v;
        do {
            s = state.get();
            v = s + 1 & VERSION_MASK;
        } while (!state.weakCompareAndSet(s, s & RUNNING_FLAG_MASK | v));  // Cancel any reservation
        return v;
    }

    private void schedule(Task task, Duration duration) {
        executor.schedule(task, duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    private final class Task implements Runnable {
        private int version;

        private Task(int version) {
            this.version = version;
        }

        @Override
        public void run() {
            int s;
            for (;;) {
                s = state.get();
                if ((s & VERSION_MASK) != version) {
                    return;
                }
                if ((s & RUNNING_FLAG_MASK) == 0) {  // No other instances are running
                    if (state.weakCompareAndSet(s, s | RUNNING_FLAG_MASK)) {
                        break;
                    }
                } else {
                    if (state.weakCompareAndSet(s, s | RESERVATION_FLAG_MASK)) {
                        return;
                    }
                }
            }
            for (;;) {
                Duration nextDelay;
                try {
                    Optional<Duration> nextDelayOptional = task.runOneIteration();
                    nextDelay = nextDelayOptional.orElse(null);
                } catch (Throwable t) {
                    nextDelay = null;
                    LOGGER.error("Unexpected exception was caught while executing scheduled task: {}", CirculationTrigger.this, t);
                }
                for (;;) {
                    s = state.get();
                    if ((s & RESERVATION_FLAG_MASK) == 0) {
                        if (state.weakCompareAndSet(s, s & ~RUNNING_FLAG_MASK)) {  // Reset running state
                            if (version == (s & VERSION_MASK) && nextDelay != null) {
                                schedule(this, nextDelay);
                            }
                            return;
                        }
                    } else {
                        if (state.weakCompareAndSet(s, s & ~RESERVATION_FLAG_MASK)) {  // Reset reservation
                            version = s & VERSION_MASK;  // Update version
                            break;
                        }
                    }
                }
            }
        }
    }
}
