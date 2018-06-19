package mingxin.wang.common.context;

import mingxin.wang.common.concurrent.AsyncMutex;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class ReducingContext<P, I> {
    private final AsyncMutex mutex;
    private final Reducible<? super P, ? extends I> reducible;
    private final Consumer<? super Reduced<? extends I>> callback;

    public ReducingContext(Executor executor,
                           Reducible<? super P, ? extends I> reducible,
                           Consumer<? super Reduced<? extends I>> callback) {
        this.mutex = AsyncMutex.on(executor);
        this.reducible = reducible;
        this.callback = callback;
    }

    public void reduce(P portion) {
        mutex.attach(() -> reducible.reduce(portion), callback);
    }
}
