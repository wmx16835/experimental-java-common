package mingxin.wang.common.concurrent;

import java.time.Duration;
import java.util.Optional;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
@FunctionalInterface
public interface CirculatingRunnable {
    Optional<Duration> runOneIteration();
}
