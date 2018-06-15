package mingxin.wang.common.context;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class NumberedReducible<P, I, E> implements Reducible<NumberedReducingPortion<P>, I> {
    private final E entirety;
    private int total = Integer.MAX_VALUE;
    private int current = 0;

    protected NumberedReducible(E initialData) {
        this.entirety = initialData;
    }

    @Override
    public Reduced<I> reduce(NumberedReducingPortion<P> portion) {
        if (portion.isLast()) {
            total = portion.getSequence();
        }
        boolean last = ++current >= total;
        portion.setSequence(current);
        portion.setLast(last);
        return Reduced.of(reduce(portion, entirety), last);
    }

    protected abstract I reduce(NumberedReducingPortion<P> portion, E entirety);
}
