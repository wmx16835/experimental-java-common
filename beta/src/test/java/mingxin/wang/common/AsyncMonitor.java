package mingxin.wang.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class AsyncMonitor<T> {
    private final BlockingQueue<Consumer<? super T>> consumers;
    private final Thread worker;

    public AsyncMonitor(T data) {
        this.consumers = new LinkedBlockingQueue<>();
        this.worker = new Thread(() -> {
            try {
                for (;;) {
                    consumers.take().accept(data);
                }
            } catch (InterruptedException ignore) {
            }
        });
        this.worker.start();
    }

    public void invoke(Consumer<? super T> consumer) {
        consumers.add(consumer);
    }

    public void stop() {
        worker.interrupt();
    }
}
