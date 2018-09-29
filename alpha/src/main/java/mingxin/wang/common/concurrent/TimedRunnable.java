package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class TimedRunnable {
    private static Logger LOGGER = LoggerFactory.getLogger(TimedRunnable.class);

    final Instant when;
    private final Callable<Optional<TimedRunnable>> what;

    public static TimedRunnable of(Instant when, Callable<Optional<TimedRunnable>> what) {
        Preconditions.checkNotNull(when);
        Preconditions.checkNotNull(what);
        return new TimedRunnable(when, what);
    }

    private TimedRunnable(Instant when, Callable<Optional<TimedRunnable>> what) {
        this.when = when;
        this.what = what;
    }

    TimedRunnable run() {
        try {
            return what.call().orElse(null);
        } catch (Throwable t) {
            LOGGER.error("Exception was caught while executing a timed task.", t);
        }
        return null;
    }
}
