package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Copyright (C) Qunar.com - All Rights Reserved.
 *
 * @author Mingxin Wang
 */
public final class TimedRunnable {
    final Instant when;
    final Callable<Optional<TimedRunnable>> what;

    public static TimedRunnable of(Instant when, Callable<Optional<TimedRunnable>> what) {
        Preconditions.checkNotNull(when);
        Preconditions.checkNotNull(what);
        return new TimedRunnable(when, what);
    }

    private TimedRunnable(Instant when, Callable<Optional<TimedRunnable>> what) {
        this.when = when;
        this.what = what;
    }
}
