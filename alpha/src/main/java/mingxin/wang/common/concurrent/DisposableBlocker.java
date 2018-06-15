package mingxin.wang.common.concurrent;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class DisposableBlocker {
    private final Thread thread = Thread.currentThread();
    private final AtomicBoolean done = new AtomicBoolean();

    public boolean unblock() {
        if (done.compareAndSet(false, true)) {
            LockSupport.unpark(thread);
            return true;
        }
        return false;
    }

    public void block() {
        checkPreconditions();
        while (!done.get()) {
            LockSupport.park();
        }
    }

    public void blockInterruptibly() throws InterruptedException {
        checkPreconditions();
        for (;;) {
            if (Thread.interrupted()) {
                if (done.compareAndSet(false, true)) {
                    throw new InterruptedException();
                }
                thread.interrupt();
                break;
            }
            if (done.get()) {
                break;
            }
            LockSupport.park();
        }
    }

    public boolean blockUntil(Instant instant) {
        Preconditions.checkNotNull(instant);
        checkPreconditions();
        long deadline = instant.toEpochMilli();
        for (;;) {
            if (Thread.interrupted() || System.currentTimeMillis() >= deadline) {
                return !done.compareAndSet(false, true);
            }
            if (done.get()) {
                return true;
            }
            LockSupport.parkUntil(deadline);
        }
    }

    public boolean blockFor(Duration duration) {
        Preconditions.checkNotNull(duration);
        return blockUntil(Instant.now().plus(duration));
    }

    private void checkPreconditions() {
        Preconditions.checkState(thread == Thread.currentThread(),
                "The thread calling this method shall be identical to the one creates this object.");
    }
}
