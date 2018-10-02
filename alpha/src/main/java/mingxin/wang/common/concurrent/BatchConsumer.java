package mingxin.wang.common.concurrent;

import com.google.common.collect.Lists;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class BatchConsumer<T> {
    private final MutexQueue<T> queue = new MutexQueue<>();
    private final AtomicInteger count = new AtomicInteger();
    private final int batchSize;
    private final Duration gap;
    private final TimedCirculation circulation;

    protected BatchConsumer(int batchSize, Duration gap, ScheduledExecutorService executor) {
        this.batchSize = batchSize;
        this.gap = gap;
        this.circulation = new TimedCirculation(executor) {
            @Override
            protected Optional<Duration> runOneIteration() {
                int scale = count.getAndSet(0);
                if (scale != 0) {
                    ArrayList<T> data = Lists.newArrayListWithCapacity(scale);
                    for (int i = 0; i < scale; ++i) {
                        data.add(queue.pull());
                    }
                    consume(data);
                }
                return Optional.empty();
            }
        };
    }

    public final void accept(T data) {
        queue.offer(data);
        int current = count.incrementAndGet();
        if (current == 1) {
            circulation.trigger(gap);
        }
        if (current == batchSize) {
            circulation.trigger();
        }
    }

    protected abstract void consume(List<T> data);
}
