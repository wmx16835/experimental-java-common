package mingxin.wang.common.concurrent;

import java.time.Instant;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public interface TimedExecutor extends AutoCloseable {
    void execute(Runnable what, Instant when);
}
