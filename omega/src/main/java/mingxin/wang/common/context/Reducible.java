package mingxin.wang.common.context;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
@FunctionalInterface
public interface Reducible<P, I> {
    Reduced<? extends I> reduce(P portion);
}
