package mingxin.wang.common.concurrent;

import com.google.common.collect.Lists;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class BatchConsumer<T> {
    private final MutexQueue<T> queue = new MutexQueue<>();
    private final AtomicInteger count = new AtomicInteger();
    private final int batchSize;
    private final Duration gap;
    private final CirculationTrigger trigger;

    protected BatchConsumer(int batchSize, Duration gap, TimedExecutor executor) {
        this.batchSize = batchSize;
        this.gap = gap;
        this.trigger = CirculationTrigger.bind(executor, () -> {
            int scale = count.getAndSet(0);
            if (scale != 0) {
                ArrayList<T> data = Lists.newArrayListWithCapacity(scale);
                for (int i = 0; i < scale; ++i) {
                    data.add(queue.pull());
                }
                consume(data);
            }
            return Optional.empty();
        });
    }

    public final void accept(T data) {
        queue.offer(data);
        int current = count.incrementAndGet();
        if (current == 1) {
            trigger.fire(Instant.now().plus(gap));
        }
        if (current == batchSize) {
            trigger.fire();
        }
    }

    protected abstract void consume(List<T> data);
}
