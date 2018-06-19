package mingxin.wang.common.context;

import com.google.common.cache.Cache;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ReducingContextService<K, P, I> {
    private Cache<K, ReducingContext<P, I>> cache;

    protected ReducingContextService(Cache<K, ReducingContext<P, I>> cache) {
        this.cache = cache;
    }

    public final void register(K key, Executor executor, Reducible<? super P, ? extends I> reducible) {
        cache.put(key, new ReducingContext<>(executor, reducible, reduced -> {
            if (reduced.isLast()) {
                deregister(key);
            }
            postProcess(key, reduced);
        }));
    }

    public final void deregister(K key) {
        cache.invalidate(key);
    }

    public final Optional<ReducingContext<P, I>> get(K key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    protected void postProcess(K key, Reduced<? extends I> result) {}
}
