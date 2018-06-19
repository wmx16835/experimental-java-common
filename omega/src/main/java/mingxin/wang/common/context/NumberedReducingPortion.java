package mingxin.wang.common.context;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class NumberedReducingPortion<T> {
    private T data;
    private int sequence;
    private boolean last;

    public NumberedReducingPortion(int sequence, boolean last, T data) {
        this.sequence = sequence;
        this.last = last;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public int getSequence() {
        return sequence;
    }

    public boolean isLast() {
        return last;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
