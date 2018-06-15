package mingxin.wang.common.context;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class Reduced<I> {
    private final I data;
    private final boolean last;

    public static <R> Reduced<R> of(R data, boolean last) {
        return new Reduced<>(data, last);
    }

    private Reduced(I data, boolean last) {
        this.data = data;
        this.last = last;
    }

    public I getData() {
        return data;
    }

    public boolean isLast() {
        return last;
    }
}
