package mingxin.wang.common.json;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class JsonDifference {
    public enum Type {
        OBJECT, ARRAY, NONE, ENTIRETY
    }

    private Type type;

    protected JsonDifference(Type type) {
        this.type = type;
    }

    public final Type getDifferenceType() {
        return type;
    }
}
