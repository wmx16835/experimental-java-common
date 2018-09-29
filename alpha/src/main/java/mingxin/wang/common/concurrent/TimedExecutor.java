package mingxin.wang.common.concurrent;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public interface TimedExecutor extends AutoCloseable {
    void execute(TimedRunnable task);
}
