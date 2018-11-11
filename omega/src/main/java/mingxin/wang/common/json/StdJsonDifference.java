package mingxin.wang.common.json;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class StdJsonDifference extends JsonDifference {
    StdJsonDifference(Type type) {
        super(type);
    }

    public ObjectJsonDifference castToObjectDifference() {
        throw new UnsupportedOperationException();
    }

    public ArrayJsonDifference castToArrayDifference() {
        throw new UnsupportedOperationException();
    }

    public NoneJsonDifference castToNoneDifference() {
        throw new UnsupportedOperationException();
    }

    public EntiretyJsonDifference castToEntiretyDifference() {
        throw new UnsupportedOperationException();
    }
}
