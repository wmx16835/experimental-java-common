package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class TimedCirculation {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimedCirculation.class);
    private static final int RESERVATION_STATE_OFFSET = 32;
    private static final long IDLE_STATE_MASK = 0x000000007FFFFFFFL;
    private static final long RUNNING_FLAG_MASK = 0x0000000080000000L;
    private static final long NO_RESERVATION_STATE_MASK = IDLE_STATE_MASK | RUNNING_FLAG_MASK;

    private final AtomicLong state;
    private final TimedExecutor executor;

    protected TimedCirculation(TimedExecutor executor) {
        this.state = new AtomicLong();
        this.executor = executor;
    }

    public final void trigger() {
        trigger(Instant.now());
    }

    public final void trigger(Instant when) {
        Preconditions.checkNotNull(when);
        executor.execute(TimedRunnable.of(when, new TimedTask(advanceVersion())));
    }

    public final void suspend() {
        advanceVersion();
    }

    protected abstract Optional<Duration> runOneIteration();

    private long advanceVersion() {
        long s, v;
        do {
            s = state.get();
            v = s + 1 & IDLE_STATE_MASK;
        } while (!state.weakCompareAndSet(s, s & RUNNING_FLAG_MASK | v));
        return v;
    }

    private final class TimedTask implements Callable<Optional<TimedRunnable>> {
        private long version;

        private TimedTask(long version) {
            this.version = version;
        }

        @Override
        public Optional<TimedRunnable> call() throws Exception {
            long s;
            for (;;) {
                s = state.get();
                if ((s & IDLE_STATE_MASK) != version) {
                    return Optional.empty();
                }
                if ((s & RUNNING_FLAG_MASK) == 0) {  // No other instances are running
                    if (state.weakCompareAndSet(s, s | RUNNING_FLAG_MASK)) {
                        break;
                    }
                } else {
                    if (state.weakCompareAndSet(s, s & NO_RESERVATION_STATE_MASK | (s << RESERVATION_STATE_OFFSET))) {
                        return Optional.empty();
                    }
                }
            }
            Duration nextDuration;
            for (;;) {
                try {
                    Optional<Duration> nextDurationOptional = runOneIteration();
                    nextDuration = nextDurationOptional.orElse(null);
                } catch (Throwable t) {
                    nextDuration = null;
                    LOGGER.error("Unexpected exception was caught while executing scheduled task: {}", TimedCirculation.this, t);
                }
                for (;;) {
                    s = state.get();
                    if ((s & NO_RESERVATION_STATE_MASK) == (s >>> RESERVATION_STATE_OFFSET)) {
                        if (state.weakCompareAndSet(s, s & NO_RESERVATION_STATE_MASK)) {  // Reset reservation
                            version = s & IDLE_STATE_MASK;  // Update version
                            break;
                        }
                    } else {
                        if (state.weakCompareAndSet(s, s & IDLE_STATE_MASK)) {  // Reset running state
                            return version == (s & IDLE_STATE_MASK) && nextDuration != null
                                    ? Optional.of(TimedRunnable.of(Instant.now().plus(nextDuration), this))
                                    : Optional.empty();
                        }
                    }
                }
            }
        }
    }
}
