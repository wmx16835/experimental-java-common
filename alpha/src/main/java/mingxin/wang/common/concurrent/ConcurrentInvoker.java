package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class ConcurrentInvoker {
    private ArrayList<Consumer<Breakpoint>> snips = new ArrayList<>();

    public boolean syncInvoke(Duration timeout) {
        Preconditions.checkNotNull(timeout);
        DisposableBlocker blocker = new DisposableBlocker();
        Instant deadline = Instant.now().plus(timeout);
        invoke(blocker::unblock);
        return blocker.blockUntil(deadline);
    }

    public void syncInvoke() {
        DisposableBlocker blocker = new DisposableBlocker();
        invoke(blocker::unblock);
        blocker.block();
    }

    public void asyncInvoke(Breakpoint breakpoint, Executor executor, Runnable callback) {
        asyncInvoke(executor, makeRecursiveCallback(breakpoint, callback));
    }

    public void asyncInvoke(Executor executor, Runnable callback) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        invoke(() -> executor.execute(callback));
    }

    public void invoke(Breakpoint breakpoint, Runnable callback) {
        invoke(makeRecursiveCallback(breakpoint, callback));
    }

    public void invoke(Runnable callback) {
        Preconditions.checkNotNull(callback);
        if (snips.isEmpty()) {
            callback.run();
        }
        Breakpoint breakpoint = new Breakpoint(snips.size(), callback);
        call(breakpoint);
    }

    public void fork(Breakpoint breakpoint) {
        Preconditions.checkNotNull(breakpoint);
        breakpoint.count.getAndAdd(snips.size());
        call(breakpoint);
    }

    public void addAll(Collection<? extends Consumer<Breakpoint>> snips) {
        Preconditions.checkNotNull(snips);
        for (Consumer<Breakpoint> candidate : snips) {
            Preconditions.checkNotNull(candidate);
        }
        this.snips.addAll(snips);
    }

    public void add(Consumer<Breakpoint> snip) {
        Preconditions.checkNotNull(snips);
        snips.add(snip);
    }

    public void add(Executor executor, Runnable task) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(task);
        add(breakpoint -> {
            try {
                executor.execute(() -> {
                    try {
                        task.run();
                    } finally {
                        breakpoint.arrive();
                    }
                });
            } catch (Throwable t) {
                breakpoint.arrive();
                throw t;
            }
        });
    }

    private void call(Breakpoint breakpoint) {
        for (Consumer<Breakpoint> snip : snips) {
            snip.accept(breakpoint);
        }
    }

    private static Runnable makeRecursiveCallback(Breakpoint upper, Runnable callback) {
        Preconditions.checkNotNull(upper);
        Preconditions.checkNotNull(callback);
        return () -> {
            try {
                callback.run();
            } finally {
                upper.arrive();
            }
        };
    }

    public static final class Breakpoint {
        private final AtomicInteger count;
        private final Runnable callback;

        private Breakpoint(int count, Runnable callback) {
            this.count = new AtomicInteger(count);
            this.callback = callback;
        }

        public void arrive() {
            if (count.getAndDecrement() == 1) {
                callback.run();
            }
        }
    }
}
